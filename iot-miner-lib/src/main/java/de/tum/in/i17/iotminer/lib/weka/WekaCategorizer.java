package de.tum.in.i17.iotminer.lib.weka;

import de.tum.in.i17.iotminer.lib.Categorizer;
import de.tum.in.i17.iotminer.lib.TweetPreprocessor;
import weka.core.SerializationHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class WekaCategorizer implements Categorizer {

    private WekaMessageClassifier wekaClassifier;

    private File dataDir;

    private String modelFile;


    // constructor for training
    public WekaCategorizer(File dataDir, String modelFile) throws Exception {

        this.dataDir = dataDir;
        this.modelFile = modelFile;

        if (modelFile.length() == 0)
            throw new Exception("Must provide name of model file (’-t <file>’).");
        try {
            init(modelFile);
        } catch (FileNotFoundException e) {
            wekaClassifier = new WekaMessageClassifier(dataDir);
        }
    }

    // constructor for using
    public WekaCategorizer(String modelFile) throws Exception {

        this.modelFile = modelFile;
        if (modelFile.length() == 0) {
            throw new Exception("Must provide name of model file (’-t <file>’).");
        }
        init(modelFile);
    }

    private void init(String modelFile) throws Exception {
        wekaClassifier = (WekaMessageClassifier) SerializationHelper.read(modelFile);
    }

    public void saveModel() throws Exception {
        System.out.println("Saving model");
        SerializationHelper.write(modelFile, wekaClassifier);
    }

    @Override
    public String categorize(String tweet)  {
        try {
            return wekaClassifier.classifyMessage(tweet);
        } catch (Exception e) {
            return "n/a";
        }
    }

    public void train() throws IOException {
        TweetPreprocessor preprocessor = new TweetPreprocessor();
        for (File file : dataDir.listFiles()) {
            String fileName = file.getName();
            String className = fileName.split("-")[1];
            className = className.substring(0, className.length() - 4);
            System.out.println("Training for class: " + className);
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                String processedLine = preprocessor.cleanTweet(line);
                this.process(processedLine, className);
            }
        }
    }

    /**
     * message Points to the file containing the message to classify or use for
     * updating the model.
     * classValue The class label of the message if model is to be updated. Omit for
     * classification of a message.
     */
    public void process(String message, String classValue) {
        try {
            if (classValue.length() != 0) {
                wekaClassifier.updateData(message, classValue);
            } else {
                wekaClassifier.classifyMessage(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

