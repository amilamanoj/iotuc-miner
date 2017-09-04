package de.tum.in.i17.iotminer.lib;

import de.tum.in.i17.iotminer.lib.opennlp.OpenNlpCategorizer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class UseCaseCategorizer {
    private Categorizer categorizer;

    private Properties properties;

    public UseCaseCategorizer() throws Exception {
        properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/app.properties"));
        categorizer = new OpenNlpCategorizer("onlp-model-s1.txt");
    }

    void processTweets() throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            Class.forName(properties.getProperty("jdbc.driver"));
            System.out.println("Connecting to database...");
            conn = DriverManager.getConnection(properties.getProperty("db.url"), properties.getProperty("db.user"), properties.getProperty("db.pass"));
            System.out.println("Getting data ...");
            stmt = conn.createStatement();
            String sql;
            sql = "SELECT tweet_text FROM `tweets` limit 1000 ";
            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String tweet = rs.getString("tweet_text");
                String category = categorizer.categorize(tweet);
                if ("noiot".equals(category))
                System.out.println(category + " : " + tweet);

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
    }

    public static void main(String[] args) throws Exception {
        new UseCaseCategorizer().processTweets();
    }
}
