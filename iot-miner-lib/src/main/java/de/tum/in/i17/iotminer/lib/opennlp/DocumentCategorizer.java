package de.tum.in.i17.iotminer.lib.opennlp;


import java.io.FileInputStream;
import java.io.IOException;

import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class DocumentCategorizer {
    private DoccatModel docCatModel;
    private DocumentCategorizerME myCategorizer;

    public DocumentCategorizer(String modelFile) throws IOException {
            docCatModel = new DoccatModel(new FileInputStream(modelFile));
            myCategorizer = new DocumentCategorizerME(docCatModel);
    }

    public String getCategory(String text) {
        double[] outcomes = myCategorizer.categorize(text.split(" "));
        String category = myCategorizer.getBestCategory(outcomes);
        System.out.println(myCategorizer.scoreMap(text.split(" ")));
        return category;
    }

    public static void main(String[] args) throws IOException {
        DocumentCategorizer categorizer = new DocumentCategorizer("opennlp-model.txt");
        String category = categorizer.getCategory("enabling access to healthcare for millions more in asia #iot #feedly #healthcare #smartcity");
        System.out.println(category);
    }
}