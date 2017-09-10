package de.tum.in.i17.iotminer.lib.performance;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;

public class PreparePerformanceFiles {

    public void generateIotFiles() throws IOException, URISyntaxException {
        File iotTestFile = new File("testIot.csv");
        File iotTrainingFile = new File("trainIot.csv");
        File fromFile = new File(this.getClass().getResource("/stem/data/step1/class-iot.csv").toURI());
        BufferedWriter writer = new BufferedWriter(new FileWriter(iotTestFile));
        List<String> lines = Files.readAllLines(fromFile.toPath());
        for (int i = 0; i < (lines.size() * 0.2); i++) {
            writer.write(lines.get(i));
            writer.newLine();
            lines.remove(i);
        }
        writer = new BufferedWriter(new FileWriter(iotTrainingFile));
        for (int i = 0; i < lines.size(); i++) {
            writer.write(lines.get(i));
            writer.newLine();
        }
    }

    public void generateNoIotFiles() throws IOException, URISyntaxException {
        File noIotTestFile = new File("testNoIot.csv");
        File noIotTrainingFile = new File("trainNoIot.csv");
        File fromFile = new File(this.getClass().getResource("/stem/data/step1/class-noiot.csv").toURI());
        BufferedWriter writer = new BufferedWriter(new FileWriter(noIotTestFile));
        List<String> lines = Files.readAllLines(fromFile.toPath());
        for (int i = 0; i < (lines.size() * 0.2); i++) {
            writer.write(lines.get(i));
            writer.newLine();
            lines.remove(i);
        }
        writer = new BufferedWriter(new FileWriter(noIotTrainingFile));
        for (int i = 0; i < lines.size(); i++) {
            writer.write(lines.get(i));
            writer.newLine();
        }
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
    }

    public void prepareTrainingData() throws IOException {
        File iotTestInput = new File("onlp-input-step1.txt");
        File iot = new File("trainIot.csv");
        File noIot = new File("trainNoIot.csv");
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
    }

    public HashMap prepareTestDataMap() throws IOException {
        HashMap TDMap = new HashMap();
        File iot = new File("testIot.csv");
        File noIot = new File("testNoIot.csv");
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
}