package de.tum.in.i17.iotminer.lib.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TweetFetcher {

    private Properties properties;
    // Pattern for recognizing a URL, based off RFC 3986
    private static final Pattern urlPattern = Pattern.compile(
            "(?:^|[\\W])((ht|f)tp(s?):\\/\\/|www\\.)"
                    + "(([\\w\\-]+\\.){1,}?([\\w\\-.~]+\\/?)*"
                    + "[\\p{Alnum}.,%_=?&#\\-+()\\[\\]\\*$~@!:/{};']*)",
            Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL);

    public TweetFetcher() throws IOException, ClassNotFoundException {
        properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/app.properties"));
        //Register JDBC driver
        Class.forName(properties.getProperty("jdbc.driver"));
    }

    public Map<String, String> getTweets(String query) throws ClassNotFoundException, SQLException, IOException {
        Map<String, String> tweets = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Getting tweets ...");
            stmt = conn.createStatement();
            rs = stmt.executeQuery(query);
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

    public Map<String, TweetInfo> getAllUseCases() throws ClassNotFoundException, SQLException, IOException {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Getting all use cases ...");
            stmt = conn.createStatement();
            String sql = "select tweet_id from use_case";

            rs = stmt.executeQuery(sql);
            Collection<String> tweetIds = new ArrayList<>();
            while (rs.next()) {
                long tweetId = rs.getLong("tweet_id");
                tweetIds.add(String.valueOf(tweetId));
            }
            return getTweetsWithInfoFromId(tweetIds);
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

        public Map<String, TweetInfo> getTweetsWithInfoFromId(Collection<String> tweetIds) throws ClassNotFoundException, SQLException, IOException {
        Map<String, TweetInfo> tweets = new HashMap<>();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Getting tweet data from id ...");
            stmt = conn.createStatement();
            String sql = "select * from tweets where tweet_id in (" + String.join(",", tweetIds) +")";

            rs = stmt.executeQuery(sql);
            while (rs.next()) {
                long tweetId = rs.getLong("tweet_id");
                String tweetText = rs.getString("tweet_text");
                Date createdAt = rs.getDate("created_at");
                String screenName = rs.getString("screen_name");
                TweetInfo info = new TweetInfo(String.valueOf(tweetId), tweetText, createdAt, screenName);

                tweets.put(String.valueOf(tweetId), info);
            }
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
    
    public void saveTopics(Map<Integer, String> topics, boolean recreateTable) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Saving topics ...");
            String sql = "insert into industry (id, name) values (?,?)";

            if (recreateTable) {
             recreateTables();
            }

            for (Map.Entry<Integer, String> topic : topics.entrySet()) {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, String.valueOf(topic.getKey()));
                preparedStatement.setString(2, topic.getValue());
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    private void recreateTables() throws SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Deleting topics ...");
            String drop1Query = "DROP TABLE IF EXISTS use_case";
            String drop2Query = "DROP TABLE IF EXISTS industry";
            String createQuery = "CREATE TABLE industry (id int(11) NOT NULL, name varchar(255) DEFAULT NULL, PRIMARY KEY (id)) ENGINE=InnoDB DEFAULT CHARSET=utf8";
            preparedStatement = conn.prepareStatement(drop1Query);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = conn.prepareStatement(drop2Query);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            preparedStatement = conn.prepareStatement(createQuery);
            preparedStatement.executeUpdate();
            preparedStatement.close();

        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }


    public void deleteTopics() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Deleting topics ...");
            String sql = "delete from industry";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }

    public void deleteUseCases() throws ClassNotFoundException, SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Deleting usecases ...");
            String sql = "truncate use_case";
            preparedStatement = conn.prepareStatement(sql);
            preparedStatement.executeUpdate();
            preparedStatement.close();
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }


    public void saveUseCases(Map<String, TweetFetcher.TweetInfo> tweetInfoMap) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        PreparedStatement preparedStatement = null;
        try {
            System.out.println("Connecting to database...");
            conn = getDbConnection();
            // Execute a query
            System.out.println("Saving use cases ...");
            String createQuery = "CREATE TABLE IF NOT EXISTS `use_case` " +
                    "(`id` bigint(20) NOT NULL AUTO_INCREMENT,`created_at` datetime DEFAULT NULL,`description` varchar(255) DEFAULT NULL," +
                    "`name` varchar(255) DEFAULT NULL,`probability` double NOT NULL,`screen_name` varchar(255) DEFAULT NULL," +
                    "`tweet` varchar(255) DEFAULT NULL,`tweet_id` varchar(255) DEFAULT NULL,`website` varchar(255) DEFAULT NULL," +
                    "`ind_id` int(11) DEFAULT NULL, PRIMARY KEY (`id`), KEY `FK3uwxkpy73xqrs4w86x1hrguu4` (`ind_id`), " +
                    "CONSTRAINT `FK3uwxkpy73xqrs4w86x1hrguu4` FOREIGN KEY (`ind_id`) REFERENCES `industry` (`id`)) DEFAULT CHARSET=utf8";
            String sql = "insert into use_case (created_at, ind_id, screen_name, tweet, tweet_id, website, probability) " +
                    "values (?,?,?,?,?,?,?)";

            preparedStatement = conn.prepareStatement(createQuery);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            for (TweetInfo info : tweetInfoMap.values()) {
                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setDate(1, new java.sql.Date(info.getCreatedAt().getTime()));
                preparedStatement.setInt(2,info.getTopicId());
                preparedStatement.setString(3,info.getScreenName());
                preparedStatement.setString(4, info.getTweetText());
                preparedStatement.setString(5,info.getTweetId());
                preparedStatement.setString(6,getUrl(info.getTweetText()));
                preparedStatement.setDouble(7, info.getTopicProbability());

                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
    }
    
    private Connection getDbConnection() throws SQLException {
        //Open connection
        return DriverManager.getConnection(properties.getProperty("db.url"),
                properties.getProperty("db.user"), properties.getProperty("db.pass"));
    }

    private String getUrl(String tweet) {
        Matcher matcher = urlPattern.matcher(tweet);
        while (matcher.find()) {
            int matchStart = matcher.start(1);
            int matchEnd = matcher.end();
            // now you have the offsets of a URL match
            return tweet.substring(matchStart, matchEnd);
        }
        return "";
    }

    public class TweetInfo {
        private String tweetId;

        private String tweetText;

        private Date createdAt;

        private String screenName;

        private String processedTweet;

        private int topicId;

        private double topicProbability;

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

        public int getTopicId() {
            return topicId;
        }

        public void setTopicId(int topicId) {
            this.topicId = topicId;
        }

        public double getTopicProbability() {
            return topicProbability;
        }

        public void setTopicProbability(double topicProbability) {
            this.topicProbability = topicProbability;
        }
    }
}
