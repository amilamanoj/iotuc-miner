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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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


    public TrainingDataPreprocessor() throws IOException {
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        TrainingDataPreprocessor preprocessor = new TrainingDataPreprocessor();
//        String content = preprocessor.getTweetsContent();
//        preprocessor.processWhole(content, new File("/Users/amilamanoj/Development/idp/data/smarthome_processed.csv"));

        List<String> tweets = preprocessor.getTweets();
        preprocessor.process(tweets, new File("/home/vishesh/TUM/SS17/IDP/smarthome_processed4.csv"));
    }

    private void processWhole(String content, File target) throws IOException {

        System.out.println("Processing...");

        Set<String> lowerCaseLines = new TreeSet<>();


        String newContent = content.toLowerCase();
        // line breaks
        newContent = newContent.replaceAll("\\\\\\n", "");
        // links
        newContent = newContent.replaceAll("http([^\\s]+)", "");
        // twitter handles
        newContent = newContent.replaceAll("@([^\\s]+)", "");

        newContent = newContent.replaceAll("[(]via", "");
        newContent = newContent.replaceAll(":\\w\\wvia", "");
        newContent = newContent.replaceAll("via \\n", "");

        newContent = newContent.replaceAll("[^\\s]+…\\n", "");
        newContent = newContent.replaceAll("[^\\s]+… \\n", "");
        newContent = newContent.replaceAll("[^\\s]+\\.\\.\\. \\n", "");
        newContent = newContent.replaceAll("…\\n", "");
        newContent = newContent.replaceAll("… \\n", "");
        newContent = newContent.replaceAll("\\.\\.\\. \\n", "");

        String[] lines = newContent.split("\n");

        Collections.addAll(lowerCaseLines, lines);

        System.out.println("Language detection...");


        BufferedWriter writer = new BufferedWriter(new FileWriter(target));
        for (String line : lowerCaseLines) {
            if (line.contains("??????")) {
                continue;
            }
            String lang = "";
            Optional<LdLocale> langOpt = detectLanguage(line);
            if (langOpt.isPresent()) {
                lang = langOpt.get().getLanguage();
            }

            if ("en".equals(lang) || lang.isEmpty()) {
                writer.write(lang + " : " + line);
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();

    }

    private void process(List<String> content, File target) throws IOException {

        System.out.println("Processing...");

        Set<String> lowerCaseLines = new TreeSet<>();


        for (String line: content) {
            if (line.contains("??????")) {
                continue;
            }
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

            if (newLine.length() < 80) {
                continue;
            }

            String lang = "";
            Optional<LdLocale> langOpt = detectLanguage(newLine);
            if (langOpt.isPresent()) {
                lang = langOpt.get().getLanguage();
            }

            if (newLine.contains("google home")) {
                System.out.println("debug");
            }

            if ("en".equals(lang)) {
                lowerCaseLines.add(newLine);

            }
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(target));
//        for (String line : lowerCaseLines) {
//            writer.write(line);
//            writer.newLine();
//        }
        Iterator<String> iter = lowerCaseLines.iterator();
        String first = iter.next();
        writer.write(first);
        writer.newLine();
        while(iter.hasNext()) {
            String second = iter.next();
            double distance = tweetSimilarity.similarity(first, second);
            if (distance < 0.5) {
                writer.write(second);
                writer.newLine();
                first = second;
            }
        }



        writer.flush();
        writer.close();

    }

    private Optional<LdLocale> detectLanguage(String tweet) throws IOException {
        //query:
        TextObject textObject = textObjectFactory.forText(tweet);
        return languageDetector.detect(textObject);
    }

    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost/twitter";

    private  List<String>  getTweets() throws ClassNotFoundException, SQLException, IOException {
        List<String> tweets = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, "root", "Welcome@01");

            //STEP 4: Execute a query
            System.out.println("Getting data ...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tweet_text FROM `tweets` " +
                    "WHERE (tweet_text like '%smarthome%' or tweet_text like '%smart home%') " +
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

    private String getTweetsContent() throws ClassNotFoundException, SQLException, IOException {
        List<String> tweets = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            //STEP 2: Register JDBC driver
            Class.forName(JDBC_DRIVER);

            //STEP 3: Open a connection
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(DB_URL, "root", "root");

            //STEP 4: Execute a query
            System.out.println("Creating statement...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tweet_text INTO OUTFILE '/Users/amilamanoj/Development/idp/data/tmp.csv' " +
                    "FIELDS TERMINATED BY ',' LINES TERMINATED BY '\\n' FROM `tweets` " +
                    "WHERE (tweet_text like '%smarthome%' or tweet_text like '%smart home%') and tweet_text like '%iot%' limit 2000;";
            rs = stmt.executeQuery(sql);
//            while (rs.next()) {
//                int id = rs.getInt("id");
//            }
            return new String(Files.readAllBytes(Paths.get("/Users/amilamanoj/Development/idp/data/tmp.csv")));
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
