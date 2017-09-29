package de.tum.in.i17.iotminer.lib;

import de.tum.in.i17.iotminer.lib.mallet.TopicModeller;
import de.tum.in.i17.iotminer.lib.opennlp.OpenNlpCategorizer;
import de.tum.in.i17.iotminer.lib.util.TweetFetcher;
import de.tum.in.i17.iotminer.lib.weka.WekaCategorizer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UseCaseIdentifier {

    private Categorizer categorizer;


    public UseCaseIdentifier(Categorizer cat) throws Exception {
        categorizer = cat;
    }

    public static void main(String[] args) throws Exception {
        Categorizer cat = new WekaCategorizer("weka-model-s1.txt");
        UseCaseIdentifier useCaseIdentifier = new UseCaseIdentifier(cat);
        useCaseIdentifier.mineUseCases();
        //useCaseIdentifier.remodelTopics(3);
    }

    private void mineUseCases() throws Exception {
        TweetFetcher fetcher = new TweetFetcher();
        Map<String, String> iotTweets = fetcher.getTweets(
                "SELECT * FROM `tweets` where tweet_text like '% iot %' or tweet_text like '%#iot%' or tweet_text like '%internet of things%' limit 10000");

        Map<String, String> iotUseCases = this.getIoTUseCases(iotTweets);
        Map<String, TweetFetcher.TweetInfo> useCaseInfoMap = fetcher.getTweetsWithInfoFromId(iotUseCases.keySet());
        TopicModeller topicModeller = new TopicModeller(6);
        topicModeller.modelTopics(iotUseCases);
        Map<Integer, String> topics = topicModeller.getTopicList();
        System.out.println(topics);
        System.out.println(iotUseCases.size());
        System.out.println(iotTweets.size());
        for (String tweetId : iotUseCases.keySet()) {
            double[] distribution = topicModeller.getTopicDistribution(tweetId);
            int topicId = topicModeller.getMaxIndex(distribution);
            double topicProbability = distribution[topicId];
            useCaseInfoMap.get(tweetId).setTopicId(topicId);
            useCaseInfoMap.get(tweetId).setTopicProbability(topicProbability);
        }
        fetcher.saveTopics(topics);
        fetcher.saveUseCases(useCaseInfoMap);
    }

    private void remodelTopics(int numberOfTopics) throws Exception {
        TweetFetcher fetcher = new TweetFetcher();
        Map<String, TweetFetcher.TweetInfo> useCaseInfoMap = fetcher.getAllUseCases();
        Map<String, String> iotUseCases = new HashMap<>();
        TweetPreprocessor preprocessor = new TweetPreprocessor();
        for (TweetFetcher.TweetInfo info : useCaseInfoMap.values()) {
            iotUseCases.put(info.getTweetId(), info.getTweetText());
        }
        Map<String, String> preProcessedIotUseCases = preprocessor.preProcess(iotUseCases);
        TopicModeller topicModeller = new TopicModeller(numberOfTopics);
        topicModeller.modelTopics(preProcessedIotUseCases);
        Map<Integer, String> topics = topicModeller.getTopicList();
        System.out.println(topics);
        for (String tweetId : iotUseCases.keySet()) {
            double[] distribution = topicModeller.getTopicDistribution(tweetId);
            int topicId = topicModeller.getMaxIndex(distribution);
            double topicProbability = distribution[topicId];
            useCaseInfoMap.get(tweetId).setTopicId(topicId);
            useCaseInfoMap.get(tweetId).setTopicProbability(topicProbability);
        }
        fetcher.deleteUseCases();
        fetcher.deleteTopics();
        fetcher.saveTopics(topics);
        fetcher.saveUseCases(useCaseInfoMap);
    }

    public Map<String, String> getIoTUseCases(Map<String, String> candidateList) throws Exception {
        TweetPreprocessor preprocessor = new TweetPreprocessor();
        Map<String, String> preProcessedTweets = preprocessor.preProcess(candidateList);

        Map<String, String> iotUseCases = new HashMap<>();
        preProcessedTweets.forEach((key, value) -> {
            String category = categorizer.categorize(value);
            if ("iot".equals(category)) {
                iotUseCases.put(key, value);
            }
        });
        return iotUseCases;
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
