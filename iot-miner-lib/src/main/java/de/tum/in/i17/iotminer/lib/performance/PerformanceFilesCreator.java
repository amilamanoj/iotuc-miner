package de.tum.in.i17.iotminer.lib.performance;

import cc.mallet.util.FileUtils;
import de.tum.in.i17.iotminer.lib.weka.WekaCategorizer;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class PerformanceFilesCreator {

    private double testPercentage;

    public PerformanceFilesCreator(double testPercentage) {
        this.testPercentage = testPercentage;
    }

    public void generateIotFiles() throws IOException, URISyntaxException {
        File iotTestFile = new File("performance/test/test-iot.txt");
        File iotTrainingFile = new File("performance/train/train-iot.txt");
        File fromFile = new File("performance/class-iot.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(iotTestFile));
        List<String> lines = Files.readAllLines(fromFile.toPath());
        for (int i = 0; i < (lines.size() * testPercentage); i++) {
            writer.write(lines.get(i));
            writer.newLine();
            lines.remove(i);
        }
        writer = new BufferedWriter(new FileWriter(iotTrainingFile));
        for (int i = 0; i < lines.size(); i++) {
            writer.write(lines.get(i));
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void generateNoIotFiles() throws IOException, URISyntaxException {
        File noIotTestFile = new File("performance/test/test-noiot.txt");
        File noIotTrainingFile = new File("performance/train/train-noiot.txt");
        File fromFile = new File("performance/class-noiot.csv");
        BufferedWriter writer = new BufferedWriter(new FileWriter(noIotTestFile));
        List<String> lines = Files.readAllLines(fromFile.toPath());
        for (int i = 0; i < (lines.size() * testPercentage); i++) {
            writer.write(lines.get(i));
            writer.newLine();
            lines.remove(i);
        }
        writer = new BufferedWriter(new FileWriter(noIotTrainingFile));
        for (int i = 0; i < lines.size(); i++) {
            writer.write(lines.get(i));
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public void prepareClassIotFile(String outFileName) throws URISyntaxException, IOException {
        File dataDir = new File(this.getClass().getResource("/stem/data/step2").toURI());
        File outFile = new File(outFileName);
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile));
        for (File file : dataDir.listFiles()) {
            //String fileName = file.getName();
            //String className = fileName.split("-")[1];
            //className = className.substring(0, className.length() - 4);
            List<String> lines = Files.readAllLines(file.toPath());
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
        writer.flush();
        writer.close();
    }

    public void prepareTrainingData() throws IOException, URISyntaxException {
        File iotTestInput = new File("performance/models/onlp-input-step1.txt");
        File iot = new File("performance/train/train-iot.txt");
        File noIot = new File("performance/train/train-noiot.txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(iotTestInput));
        List<String> iotLines = Files.readAllLines(iot.toPath());
        List<String> noIotLines = Files.readAllLines(noIot.toPath());
        for (String line : iotLines) {
            writer.write("iot " + line);
            writer.newLine();
        }
        for (String line : noIotLines) {
            writer.write("noiot " + line);
            writer.newLine();
        }
        writer.flush();
        writer.close();
    }

    public HashMap prepareTestDataMap() throws IOException, URISyntaxException {
        HashMap TDMap = new HashMap();
        File iot = new File("performance/test/test-iot.txt");
        File noIot = new File("performance/test/test-noiot.txt");
        List<String> iotLines = Files.readAllLines(iot.toPath());
        List<String> noIotLines = Files.readAllLines(noIot.toPath());
        for (String line : iotLines) {
            TDMap.put(line, "iot");
        }
        for (String line : noIotLines) {
            TDMap.put(line, "noiot");
        }
        return TDMap;
    }

    public void generateWekaModel() throws Exception {
        WekaCategorizer classifier = new WekaCategorizer(
                new File("performance/train"), "performance/models/model-s1.txt");
        classifier.train();
    }

    public void createDirs() {
        new File("performance/models").mkdir();
        new File("performance/test").mkdir();
        new File("performance/train").mkdir();
    }

    public void deleteDir(File dir) {
        if (dir.isDirectory()) {
            for (File sub : dir.listFiles()) {
                deleteDir(sub);
            }
        }
        dir.delete();
    }
}