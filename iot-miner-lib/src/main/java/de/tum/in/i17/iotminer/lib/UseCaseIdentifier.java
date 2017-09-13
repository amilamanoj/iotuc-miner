package de.tum.in.i17.iotminer.lib;

import de.tum.in.i17.iotminer.lib.opennlp.OpenNlpCategorizer;
import de.tum.in.i17.iotminer.lib.util.TweetFetcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UseCaseIdentifier {

    private Categorizer categorizer;


    public UseCaseIdentifier(Categorizer cat) throws Exception {
        categorizer = cat;
    }

    public static void main(String[] args) throws Exception {
        TweetFetcher fetcher = new TweetFetcher();
        Categorizer cat = new OpenNlpCategorizer("model-s1.txt");
        UseCaseIdentifier useCaseCategorizer = new UseCaseIdentifier(cat);
        Map<String, String> iotTweets = fetcher.getTweets(
                "SELECT * FROM `tweets` where tweet_text like '% iot %' or tweet_text like '%#iot%' or tweet_text like '%internet of things%' limit 1000");

        Map<String, String> iotUseCases = useCaseCategorizer.getIoTUseCases(iotTweets);
        for (String useCase : iotUseCases.values()) {
            System.out.println(useCase);
        }
    }

    public Map<String, String> getIoTUseCases(Map<String, String> candidateList) throws Exception {
        TweetPreprocessor preprocessor = new TweetPreprocessor();
        Map<String, String> preProcessedTweets = preprocessor.preProcess(candidateList);

        Map<String, String> iotUseCases = new HashMap<>();
        for (Map.Entry<String, String> candidate : preProcessedTweets.entrySet()) {
            String category = categorizer.categorize(candidate.getValue());
            if ("iot".equals(category)) {
                iotUseCases.put(candidate.getKey(), candidate.getValue());
            }
        }
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
