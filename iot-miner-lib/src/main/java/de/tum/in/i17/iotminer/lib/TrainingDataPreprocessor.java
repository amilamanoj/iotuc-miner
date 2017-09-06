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
        preprocessor.process(tweets, new File("/Users/amilamanoj/Development/idp/data/class-connectedcar.csv"));
    }

    private void process(List<String> content, File target) throws IOException {

        System.out.println("Processing...");

        Set<String> lowerCaseLines = new TreeSet<>();

        for (String line: content) {
            if (line.contains("??????")) {
                continue;
            }
            String newLine = cleanTweet(line);

            if (newLine.length() < 80) {
                continue;
            }

            String lang =  detectLanguage(newLine);
            if ("en".equals(lang)) {
                lowerCaseLines.add(newLine);
            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(target));

        Iterator<String> iter = lowerCaseLines.iterator();
        String first = iter.next();
        writer.write(first);
        writer.newLine();
        while(iter.hasNext()) {
            String second = iter.next();
            double distance = TweetSimilarity.similarity(first, second);
            if (distance < 0.5) {
                writer.write(second);
                writer.newLine();
                first = second;
            }
        }

        writer.flush();
        writer.close();

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
            sql = "SELECT tweet_text FROM `tweets` " +
                    "WHERE (tweet_text like '%connected car%' or tweet_text like '%connectedcar%') " +
                    " or (tweet_text like '%smart car%' or tweet_text like '%smartcar%') " +
                    " or (tweet_text like '%driverless%') " +
                    "and tweet_text like '%iot%'";
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
