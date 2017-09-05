package de.tum.in.i17.iotminer.lib.opennlp;

import opennlp.tools.doccat.DoccatFactory;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;
import opennlp.tools.doccat.DocumentSample;
import opennlp.tools.doccat.DocumentSampleStream;
import opennlp.tools.util.MarkableFileInputStreamFactory;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.TrainingParameters;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

public class ModelTrainer {
    public static void main(String[] args) throws URISyntaxException, IOException {
        new ModelTrainer().prepareTrainingFileStep1("onlp-input-step1.txt");
        new ModelTrainer().prepareTrainingFileStep2("onlp-input-step2.txt");
        new ModelTrainer().trainModel("onlp-input-step1.txt", "onlp-model-s1.txt");
        new ModelTrainer().trainModel("onlp-input-step2.txt", "onlp-model-s2.txt");
    }

    public void trainModel(String inputFile, String modelFile) {
        DoccatModel model = null;
        try {

            MarkableFileInputStreamFactory factory = new MarkableFileInputStreamFactory(
                    new File(inputFile));
            ObjectStream<String> lineStream = new PlainTextByLineStream(
                    factory, "UTF-8");

            ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(
                    lineStream);

            model = DocumentCategorizerME.train("en", sampleStream,
                                                TrainingParameters.defaultParams(), new DoccatFactory());

            OutputStream modelOut = null;
            File modelFileTmp = new File(modelFile);
            modelOut = new BufferedOutputStream(new FileOutputStream(
                    modelFileTmp));
            model.serialize(modelOut);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void prepareTrainingFileStep1(String outFileName) throws URISyntaxException, IOException {

        // Collect all IoT related tweets from step2 data and write them to the training file
        File dataDir = new File(this.getClass().getResource("/supervised/data/step2").toURI());
        File outFile = new File(outFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        for (File file : dataDir.listFiles()) {
            String fileName = file.getName();
            String className = fileName.split("-")[1];
            //className = className.substring(0, className.length() - 4);
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                writer.write("iot " + line);
                writer.newLine();
            }
        }

        // Add non-IoT tweets to the training file
        List<String> noIotlines = Files.readAllLines(
                new File(this.getClass().getResource("/supervised/data/step1/class-noiot.csv").toURI()).toPath());
        for (String line : noIotlines) {
            writer.write("noiot " + line);
            writer.newLine();
        }

        writer.flush();
        writer.close();
    }

    public void prepareTrainingFileStep2(String outFileName) throws URISyntaxException, IOException {
        File dataDir = new File(this.getClass().getResource("/supervised/data/step2").toURI());
        File outFile = new File(outFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));

        for (File file : dataDir.listFiles()) {
            String fileName = file.getName();
            String className = fileName.split("-")[1];
            className = className.substring(0, className.length() - 4);
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                writer.write(className + " " + line);
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }
}
