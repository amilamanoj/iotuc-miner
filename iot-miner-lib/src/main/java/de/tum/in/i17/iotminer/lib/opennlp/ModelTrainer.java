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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ModelTrainer {
    public static void main(String[] args) {
        new ModelTrainer().trainModel(args[0], args[1]);
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
}
