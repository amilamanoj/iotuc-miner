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
        List<String> tweets = preprocessor.getTweets();
        preprocessor.process(tweets, new File("class-iot.txt"));
    }

    private void process(List<String> content, File target) throws IOException {

        System.out.println("Processing content: " + content.size());
        TreeSet<String> sortedLines = new TreeSet<>();

        for (String line: content) {
            if (line.contains("??????")) {
                continue;
            }
            String cleanedLine = cleanTweet(line);

            if (cleanedLine.length() < 80) {
                continue;
            }

            String lang =  detectLanguage(cleanedLine);
            if (!"en".equals(lang)) {
                continue;
            }

            String resultLine = performSWRAndStemming(cleanedLine);
            sortedLines.add(resultLine);
        }
        System.out.println("After removing short and non-english text: " + sortedLines.size());

        BufferedWriter writer = new BufferedWriter(new FileWriter(target));

        List<String> writtenLines = new ArrayList<>();
        String firstLine = sortedLines.pollFirst();
        writer.write(firstLine);
        writtenLines.add(firstLine);
        for (String line : sortedLines) {
            boolean shouldWrite = true;
            for (String writtenLine : writtenLines) {
                double similarity = TweetSimilarity.similarity(line, writtenLine);
                if (similarity > 0.5) {
                    shouldWrite = false;
                }
            }
            if (shouldWrite) {
                writer.write(line);
                writer.newLine();
                writtenLines.add(line);
            }
        }
        writer.flush();
        writer.close();

        System.out.printf("After removing duplicates: " + writtenLines.size());
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
        newLine = newLine.replaceAll("[0-9]","");
        return newLine;
    }

    private String detectLanguage(String tweet) throws IOException {
        //query:
        TextObject textObject = textObjectFactory.forText(tweet);
        Optional<LdLocale>  res = languageDetector.detect(textObject);
        if (res.isPresent()) {
            return res.get().getLanguage();
        } else {
            return "n/a";
        }
    }

    private  List<String>  getTweets() throws ClassNotFoundException, SQLException, IOException {
        List<String> tweets = new ArrayList<>();
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
            String sql;
            sql = "SELECT tweet_text FROM `tweets` where tweet_text like '% iot %' or tweet_text like '%#iot%' or tweet_text like '%internet of things%'";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                tweets.add(rs.getString("tweet_text"));
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

}
