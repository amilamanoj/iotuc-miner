package de.tum.in.i17.iotminer.lib.weka;

import de.tum.in.i17.iotminer.lib.Trainer;

import java.io.File;

public class WekaModelTrainer implements Trainer{
    @Override
    public void trainStep1() {
        try {
            WekaCategorizer classifier = new WekaCategorizer(
                    new File(WekaCategorizer.class.getResource("/supervised/data/step1").toURI()), "weka-model-s1.txt");
            classifier.train();
            classifier.process("enabling access to for millions more in asia feedly smartcity", "");
            classifier.saveModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void trainStep2() {
        try {
            WekaCategorizer classifier = new WekaCategorizer(
                    new File(WekaCategorizer.class.getResource("/supervised/data/step2").toURI()), "weka-model-s2.txt");
            classifier.train();
            classifier.process("enabling access to for millions more in asia feedly smartcity", "");
            classifier.saveModel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
