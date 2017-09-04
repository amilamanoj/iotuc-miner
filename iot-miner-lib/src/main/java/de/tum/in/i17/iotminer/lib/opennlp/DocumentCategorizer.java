package de.tum.in.i17.iotminer.lib.opennlp;


import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Objects;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class DocumentCategorizer {
    private InputStream inputStream;

    private DoccatModel docCatModel;

    private DocumentCategorizerME myCategorizer;

    public DocumentCategorizer(String modelFile) {
        Objects.nonNull(modelFile);
        initModel(modelFile);
    }

    private void initModel(String modelFile) {
        try {
            inputStream = new FileInputStream(modelFile);
            docCatModel = new DoccatModel(inputStream);
            myCategorizer = new DocumentCategorizerME(docCatModel);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

    public String getCategory(String text) {
        double[] outcomes = myCategorizer.categorize(text.split(" "));
        String category = myCategorizer.getBestCategory(outcomes);
        System.out.println(myCategorizer.scoreMap(text.split(" ")));

        return category;
    }

    public static void main(String[] args) {
        DocumentCategorizer categorizer = new DocumentCategorizer("opennlp-model.txt");
        String category = categorizer.getCategory("enabling access to healthcare for millions more in asia #iot #feedly #healthcare #smartcity");
        System.out.println(category);
    }
}