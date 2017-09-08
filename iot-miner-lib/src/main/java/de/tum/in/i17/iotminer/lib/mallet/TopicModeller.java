package de.tum.in.i17.iotminer.lib.mallet;

import cc.mallet.pipe.*;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicAssignment;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.*;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class TopicModeller {

    ParallelTopicModel model;
    InstanceList instances;

    public static void main(String[] args) throws IOException {
        TopicModeller modeller = new TopicModeller(10);
        modeller.modelTopics("class-iot-mallet.txt");
        modeller.inferTopic("singapore to launch first trial of driverless buses in jurong west selfdrivingcars iot transport smartcars");
    }

    public TopicModeller(int numberOfTopics) {
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
        model = new ParallelTopicModel(numberOfTopics, 1.0, 0.01);

    }

    public void modelTopics(String fileName) throws IOException {

        Reader fileReader = new InputStreamReader(new FileInputStream(new File(fileName)), "UTF-8");
        instances.addThruPipe(new CsvIterator(fileReader, Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"),
                3, 2, 1)); // data, label, name fields

        model.addInstances(instances);

        // Use two parallel samplers, which each look at one half the corpus and combine
        //  statistics after every iteration.
        model.setNumThreads(2);

        // Run the model for 500 iterations and stop (this is for testing only,
        //  for real applications, use 1000 to 2000 iterations)
        model.setNumIterations(500);
        model.estimate();

//         Show the words and topics in the first instance
//        // The data alphabet maps word IDs to strings
//        Alphabet dataAlphabet = instances.getDataAlphabet();
//
//        FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
//        LabelSequence topics = model.getData().get(0).topicSequence;
//
//        Formatter out = new Formatter(new StringBuilder(), Locale.US);
//        for (int position = 0; position < tokens.getLength(); position++) {
//            out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)), topics.getIndexAtPosition(position));
//        }
//        System.out.println(out);
//
//        // Estimate the topic distribution of the first instance,
//        //  given the current Gibbs state.
//        double[] topicDistribution = model.getTopicProbabilities(0);
//
//        // Get an array of sorted sets of word ID/count pairs
//        ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();
//
//        // Show top 5 words in topics with proportions for the first document
//        for (int topic = 0; topic < model.getNumTopics(); topic++) {
//            Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
//            out = new Formatter(new StringBuilder(), Locale.US);
//            out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
//            int rank = 0;
//            while (iterator.hasNext() && rank < 5) {
//                IDSorter idCountPair = iterator.next();
//                out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
//                rank++;
//            }
//            System.out.println(out);
//        }

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