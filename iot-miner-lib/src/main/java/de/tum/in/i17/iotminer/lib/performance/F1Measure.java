package de.tum.in.i17.iotminer.lib.performance;

import de.tum.in.i17.iotminer.lib.UseCaseCategorizer;
import de.tum.in.i17.iotminer.lib.opennlp.ModelTrainer;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class F1Measure {
    public static void main(String[] args) throws Exception {
        F1Measure f1 = new F1Measure();
        PerformanceFilesCreator ppf = new PerformanceFilesCreator(0.2);

        ppf.generateIotFiles();
        ppf.generateNoIotFiles();
        ppf.prepareTrainingData();

        new ModelTrainer().trainModel("onlp-input-step1.txt", "onlp-model-s1.txt");

        UseCaseCategorizer useCaseCategorizer = new UseCaseCategorizer();
        List<String> iotTweets = f1.getTweets();
        Map<String, String> classificationMap = useCaseCategorizer.classifyTweets(iotTweets);
        Map<String, String> testDataMap = ppf.prepareTestDataMap();
        f1.compareMap(classificationMap, testDataMap);
    }

    public List<String> getTweets() throws URISyntaxException, IOException {
        File iot = new File("testIot.csv");
        File noIot = new File("testNoIot.csv");
        List<String> iotTweets = Files.readAllLines(iot.toPath());
        List<String> noIotTweets = Files.readAllLines(noIot.toPath());
        List<String> iotTestTweets = new ArrayList<>();
        iotTestTweets.addAll(iotTweets);
        iotTestTweets.addAll(noIotTweets);
        return iotTestTweets;
    }

    public void compareMap(Map<String, String> classification, Map<String, String> original) {
        int truePositive = 0;
        int falsePositive = 0;
        int falseNegative = 0;

        System.out.println(classification.size());
        System.out.println(original.size());
        Iterator itCM = classification.entrySet().iterator();
        while (itCM.hasNext()) {
            Map.Entry result = (Map.Entry) itCM.next();
            if (original.get(result.getKey()).equals("iot") && result.getValue().equals("iot")) {
                truePositive++;
            }
            if (original.get(result.getKey()).equals("noiot") && result.getValue().equals("iot")) {
                falsePositive++;
            }
            if (original.get(result.getKey()).equals("iot") && result.getValue().equals("noiot")) {
                falseNegative++;
            }
            //System.out.println(result.getKey() + " Classification " + result.getValue() + " Original " + original.get(result.getKey()));
        }
        System.out.println("True positives: " + truePositive);
        System.out.println("False positives: " + falsePositive);
        System.out.println("False negatives: " + falseNegative);
        F1ScoreCalculate(truePositive, falsePositive, falseNegative);
    }

    public void F1ScoreCalculate(int truePositive, int falsePositive, int falseNegative) {
        double tp = truePositive;
        double fp = falsePositive;
        double fn = falseNegative;
        double precision = tp / (tp + fp);
        double recall = tp / (tp + fn);
        double f1Score = 2 * ((precision * recall) / (precision + recall));

        System.out.println("precision: " + precision);
        System.out.println("recall: " + recall);
        System.out.println("F1 Score is: " + f1Score);
    }

}
