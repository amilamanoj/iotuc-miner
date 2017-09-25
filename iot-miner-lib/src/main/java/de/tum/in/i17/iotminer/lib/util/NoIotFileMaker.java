package de.tum.in.i17.iotminer.lib.util;

import opennlp.tools.tokenize.WhitespaceTokenizer;
import weka.core.stopwords.AbstractStopwords;
import weka.core.stopwords.Rainbow;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoIotFileMaker {
    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException, URISyntaxException {
//        TweetFetcher fetcher = new TweetFetcher();
        NoIotFileMaker nfm = new NoIotFileMaker();
//        Map<String, String> tweets = fetcher.getTweets(
//                "SELECT * FROM `tweets` where tweet_text like '% iot %' or tweet_text like '%#iot%' or tweet_text like '%internet of things%' limit 40000");
//        TweetPreprocessor preprocessor = new TweetPreprocessor();
//        Map<String, String> preProcessedTweets = preprocessor.preProcess(tweets);
//        preprocessor.writeToFile(preProcessedTweets, new File("class-noiot.csv"));
        nfm.noIotCheckWithIotFile("class-noIotFinal.csv");
    }

    public void noIotCheckWithIotFile(String outFileName) throws URISyntaxException, IOException {
        File iot = new File(this.getClass().getResource("/supervised/data/step1/class-iot.txt").toURI());
        File noIot = new File("class-noiot.csv");
        File outFile = new File(outFileName);
        List<String> iotLines = Files.readAllLines(iot.toPath());
        List<String> noIotLines = Files.readAllLines(noIot.toPath());
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        for (String noIotline : noIotLines) {
            boolean shouldWrite = true;
            for (String iotLine : iotLines) {
                double similarity = TweetSimilarity.similarity(noIotline, iotLine);
                if (similarity > 0.5) {
                    shouldWrite = false;
                    break;
                }
            }
            if (shouldWrite) {
                writer.write(noIotline);
                writer.newLine();
            }
        }

    }

    public void combineIoTCategories(String outFileName) throws URISyntaxException, IOException {

        // Collect all IoT related tweets from step2 data and write them to the training file
        File dataDir = new File(this.getClass().getResource("/supervised/data/step2").toURI());
        File outFile = new File(outFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        AbstractStopwords rainbow = new Rainbow();

        for (File file : dataDir.listFiles()) {
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String[] tokens = WhitespaceTokenizer.INSTANCE.tokenize(line);
                List<String> nonStopWords = new ArrayList<>();
                for (String token : tokens) {
                    if (!rainbow.isStopword(token)) {
                        nonStopWords.add(token);
                    }
                }
                writer.write(String.join(" ", nonStopWords));
                writer.newLine();
            }
        }


        writer.flush();
        writer.close();
    }
}
