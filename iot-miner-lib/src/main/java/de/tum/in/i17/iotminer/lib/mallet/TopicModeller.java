package de.tum.in.i17.iotminer.lib.mallet;

import cc.mallet.pipe.*;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Pattern;

public class TopicModeller {

    private int iterations;

    private ParallelTopicModel model;
    private InstanceList instances;
    private Map<String, Integer> dataMap = new HashMap<>();


    public static void main(String[] args) throws IOException, URISyntaxException {
        TopicModeller modeller = new TopicModeller(10, 500);
        List<String> lines = Files.readAllLines(new File(TopicModeller.class.getResource("/supervised/data/step1/class-iot.txt").toURI()).toPath());
        Map<String, String> tweetMap = new HashMap<>();
        for (int x = 0 ; x < lines.size() ; x++) {
            tweetMap.put(String.valueOf(x), lines.get(x));
        }
        modeller.modelTopics(tweetMap);
        modeller.getTopicList();
        modeller.inferTopic("singapore to launch first trial of driverless buses in jurong west self driving cars iot transport smartcars");
    }

    public TopicModeller(int numberOfTopics, int iterations) {
        // Begin by importing documents from text to feature sequences
        ArrayList<Pipe> pipeList = new ArrayList<Pipe>();
        // Pipes: lowercase, tokenize, remove stopwords, map to features
        pipeList.add(new CharSequenceLowercase());
        pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
        pipeList.add(new TokenSequenceRemoveStopwords());
        pipeList.add(new TokenSequence2FeatureSequence());

        instances = new InstanceList(new SerialPipes(pipeList));

        // Create a model with given number of topics, alpha_t = 0.01, beta_w = 0.01
        //  Note that the first parameter is passed as the sum over topics, while
        //  the second is
        model = new ParallelTopicModel(numberOfTopics, 5, 0.01);
        model.setOptimizeInterval(45);
        model.setBurninPeriod(200);
        this.iterations = iterations;
    }

    public void modelTopics(Map<String, String> tweets) throws IOException, URISyntaxException {

      //  Reader fileReader = new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8");
      //  instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
      //          3, 2, 1)); // data, label, name fields

        int k = 0;
        for (Map.Entry<String, String> tweet : tweets.entrySet()) {
            instances.addThruPipe(new Instance(tweet.getValue(), null, tweet.getKey(), null));
            dataMap.put(tweet.getKey(), k);
            k++;
        }

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 500 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(iterations);
        model.estimate();
    }

    public double[] getTopicDistribution(String tweetId) {
       return model.getTopicProbabilities(dataMap.get(tweetId));

    }

    public int getMaxIndex(double[] array) {
        int maxAt = 0;
        for (int i = 0; i < array.length; i++) {
            maxAt = array[i] > array[maxAt] ? i : maxAt;
        }
        return maxAt;
    }

    public Map<Integer, String> getTopicList() {
        Map<Integer, String> topicMap = new HashMap<>();
        // Show the words and topics in the first instance
        // The data alphabet maps word IDs to strings
        Alphabet dataAlphabet = instances.getDataAlphabet();
        //
        //        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
        //        LabelSequence topics = model.getData().get(0).topicSequence;
        //
        Formatter out = new Formatter(new StringBuilder(), Locale.US);
        //        for (int position = 0; position < tokens.getLength(); position++) {
        //            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
        //        }
        //        System.out.println(out);
        //
        // Estimate the topic distribution of the first instance,
        //  given the current Gibbs state.
        //double[] topicDistribution = model.getTopicProbabilities( 0);

        // Get an array of sorted sets of word ID/count pairs
        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
        Object[][] topWords = model.getTopWords(5);
        //
        // Show top 5 words in topics with proportions for the first document
        for (int topic = 0; topic < model.getNumTopics(); topic++) {
            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
            out = new Formatter(new StringBuilder(), Locale.US);
            //out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
            int rank = 0;
            while (iterator.hasNext() && rank < 5) {
                IDSorter idCountPair = iterator.next();
                out.format("%s:%.0f ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
                rank++;
            }
            topicMap.put(topic, out.toString());
        }
        return topicMap;
    }

    public void inferTopic(String input) {
        //String topicZeroText = "connected agriculture = higher yields from crops + return of unused farmland. more iot in 10 years";
        // Create a new instance named "test instance" with empty target and source fields.
        InstanceList testing = new InstanceList(instances.getPipe());
        testing.addThruPipe(new Instance(input, null, "test instance", null));

        TopicInferencer inferencer = model.getInferencer();
        double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);
        System.out.println(Arrays.toString(testProbabilities));
    }

}