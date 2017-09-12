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
import de.tum.in.i17.iotminer.lib.opennlp.OpenNlpCategorizer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class UseCaseCategorizer {

    private Categorizer categorizer;

    private Properties properties;

    private LanguageDetector languageDetector;

    private TextObjectFactory textObjectFactory;


    public UseCaseCategorizer(Categorizer cat) throws Exception {
        properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/app.properties"));
        categorizer = cat;
        List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAllBuiltIn();
        languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        textObjectFactory = CommonTextObjectFactories.forDetectingShortCleanText();
    }

    public static void main(String[] args) throws Exception {
        Categorizer cat = new OpenNlpCategorizer("model-s1.txt");
        UseCaseCategorizer useCaseCategorizer = new UseCaseCategorizer(cat);
        List<String> iotTweets = useCaseCategorizer.getIotTweets();
        useCaseCategorizer.classifyTweets(iotTweets);

    }

    List<String> getIotTweets() throws SQLException {
        List<String> iotTweets = new ArrayList<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(properties.getProperty("db.url"), properties.getProperty("db.user"),
                                               properties.getProperty("db.pass"));
            System.out.println("Getting data ...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tweet_text FROM `tweets` where tweet_text like '%#iot%' limit 10000 ";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tweet = rs.getString("tweet_text");
                String category = categorizer.categorize(tweet);
                if ("iot".equals(category)) {
                    TextObject textObject = textObjectFactory.forText(tweet);
                    String lang = "";
                    Optional<LdLocale> langOpt = languageDetector.detect(textObject);
                    if (langOpt.isPresent()) {
                        lang = langOpt.get().getLanguage();
                    }
                    if ("en".equals(lang)) {
                        iotTweets.add(tweet);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
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
        return iotTweets;
    }

    public Map<String, String> classifyTweets(List<String> tweets) throws Exception {
        Map<String, String> classificationMap = new HashMap<>();
        for (String tweet : tweets) {
            String category = categorizer.categorize(tweet);
            System.out.println(category + " : " + tweet);
            System.out.println("====================");
            classificationMap.put(tweet, category);
        }
        System.out.println("Total IoT usecases: " + tweets.size());
        return classificationMap;
    }
}
