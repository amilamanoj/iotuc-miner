package de.tum.in.i17.iotminer.lib;

import com.google.common.base.Optional;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import com.optimaize.langdetect.text.CommonTextObjectFactories;
import com.optimaize.langdetect.text.TextObject;
import com.optimaize.langdetect.text.TextObjectFactory;
import de.tum.in.i17.iotminer.lib.util.TweetSimilarity;
import opennlp.tools.stemmer.Stemmer;
import opennlp.tools.stemmer.snowball.SnowballStemmer;
import opennlp.tools.tokenize.WhitespaceTokenizer;
import weka.core.stopwords.AbstractStopwords;
import weka.core.stopwords.Rainbow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class TrainingDataPreprocessor {

    //build language detector:
    private LanguageDetector languageDetector;
    //create a text object factory
    private TextObjectFactory textObjectFactory;

    private Properties properties;


    public TrainingDataPreprocessor() throws IOException {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
        properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/app.properties"));
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        TrainingDataPreprocessor preprocessor = new TrainingDataPreprocessor();
        Map<String, String> tweets = preprocessor.getTweets("SELECT * FROM `tweets` where tweet_text like '% iot %' or tweet_text like '%#iot%' or tweet_text like '%internet of things%' limit 1000");
        Map<String, String> preProcessedTweets = preprocessor.preProcess(tweets);
        preprocessor.writeToFile(preProcessedTweets, new File("class-iot.txt"));
    }

    private Map<String, String> preProcess(Map<String, String> content) throws IOException {

        System.out.println("Processing content: " + content.size());
        TreeMap<String, String> sortedLines = new TreeMap<>();

        for (Map.Entry<String, String> line : content.entrySet()) {
            if (line.getValue().contains("??????")) {
                continue;
            }
            String cleanedLine = cleanTweet(line.getValue());

            if (cleanedLine.length() < 70) {
                continue;
            }

            String lang = detectLanguage(cleanedLine);
            if (!"en".equals(lang)) {
                continue;
            }

            String resultLine = performSWRAndStemming(cleanedLine);
            sortedLines.put(line.getKey(), resultLine);
        }
        System.out.println("After removing short and non-english text: " + sortedLines.size());

        Map<String, String> linesToWrite = new HashMap<>();
        Map.Entry<String, String> firstLine = sortedLines.pollFirstEntry();
        linesToWrite.put(firstLine.getKey(), firstLine.getValue());

        for (Map.Entry<String, String> line : sortedLines.entrySet()) {
            boolean shouldWrite = true;
            for (String writtenLine : linesToWrite.values()) {
                double similarity = TweetSimilarity.similarity(line.getValue(), writtenLine);
                if (similarity > 0.5) {
                    shouldWrite = false;
                    break;
                }
            }
            if (shouldWrite) {
                linesToWrite.put(line.getKey(), line.getValue());
            }
        }
        System.out.printf("After removing duplicates: " + linesToWrite.size());

        return linesToWrite;

    }

    private void writeToFile(Map<String, String> linesToWrite, File target) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(target));

        for (Map.Entry<String, String> line : linesToWrite.entrySet()) {
            String lineToWrite = String.join(",", line.getKey(), line.getValue());
            writer.write(lineToWrite);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    private String performSWRAndStemming(String cleanedLine) {
        String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(cleanedLine);

        List<String> nonStopWords = new ArrayList<>();
        AbstractStopwords rainbow = new Rainbow();
        for (String token : tokens) {
            if (!rainbow.isStopword(token)) {
                nonStopWords.add(token);
            }
        }

        List<String> stemmedWords = new ArrayList<>();
        Stemmer stemmer = new SnowballStemmer(SnowballStemmer.ALGORITHM.ENGLISH);
        for (String token : nonStopWords) {
            stemmedWords.add(stemmer.stem(token).toString());
        }
        return String.join(" ", stemmedWords);
    }

    private String cleanTweet(String line) {
        String newLine = line.trim();

        newLine = newLine.toLowerCase();

        // line breaks
        newLine = newLine.replaceAll("\\n", "");
        // links
        newLine = newLine.replaceAll("http([^\\s]+)", "");
        // twitter handles
        newLine = newLine.replaceAll("via @([^\\s]+)", "");
        newLine = newLine.replaceAll("@([^\\s]+)", "");

        newLine = newLine.replaceAll("[(]via", "");
        newLine = newLine.replaceAll(":\\w\\wvia", "");

        newLine = newLine.replaceAll("[^\\s]+…", "");
        newLine = newLine.replaceAll("[^\\s]+… ", "");
        newLine = newLine.replaceAll("[^\\s]+\\.\\.\\. ", "");
        newLine = newLine.replaceAll("…", "");
        newLine = newLine.replaceAll("… ", "");
        newLine = newLine.replaceAll("\\.\\.\\. ", "");
        newLine = newLine.replaceAll(" +", " ");
        newLine = newLine.replaceAll("[0-9]", "");
        return newLine;
    }

    private String detectLanguage(String tweet) throws IOException {
        //query:
        TextObject textObject = textObjectFactory.forText(tweet);
        Optional<LdLocale> res = languageDetector.detect(textObject);
        if (res.isPresent()) {
            return res.get().getLanguage();
        } else {
            return "n/a";
        }
    }

    private Map<String, String> getTweets(String query) throws ClassNotFoundException, SQLException, IOException {
        Map<String, String> tweets = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            //Register JDBC driver
            Class.forName(properties.getProperty("jdbc.driver"));
            //Open connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(properties.getProperty("db.url"),
                                               properties.getProperty("db.user"), properties.getProperty("db.pass"));
            // Execute a query
            System.out.println("Getting data ...");
            stmt = conn.createStatement();
            String sql = query;

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long tweetId = rs.getLong("tweet_id");
                String tweetText = rs.getString("tweet_text");
                tweets.put(String.valueOf(tweetId), tweetText);
            }
            System.out.println("Results: " + tweets.size());
            return tweets;
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
            if (conn != null) {
                conn.close();
            }
        }
    }

    class TweetInfo {
        private String tweetId;

        private String tweetText;

        private Date createdAt;

        private String screenName;

        private String processedTweet;

        public TweetInfo() {
        }

        public TweetInfo(String tweetId, String tweetText, Date createdAt, String screenName) {
            this.tweetId = tweetId;
            this.tweetText = tweetText;
            this.createdAt = createdAt;
            this.screenName = screenName;
        }

        public String getTweetId() {
            return tweetId;
        }

        public void setTweetId(String tweetId) {
            this.tweetId = tweetId;
        }

        public String getTweetText() {
            return tweetText;
        }

        public void setTweetText(String tweetText) {
            this.tweetText = tweetText;
        }

        public Date getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
        }

        public String getScreenName() {
            return screenName;
        }

        public void setScreenName(String screenName) {
            this.screenName = screenName;
        }

        public String getProcessedTweet() {
            return processedTweet;
        }

        public void setProcessedTweet(String processedTweet) {
            this.processedTweet = processedTweet;
        }
    }

}
