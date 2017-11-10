package de.tum.in.i17.iotminer.lib.opennlp;


import java.io.FileInputStream;

import de.tum.in.i17.iotminer.lib.Categorizer;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.doccat.DocumentCategorizerME;

public class OpenNlpCategorizer implements Categorizer {

    private DocumentCategorizerME myCategorizer;

    public OpenNlpCategorizer(String modelFile) throws Exception {
        init(modelFile);
    }

    private void init(String model) throws Exception {
        DoccatModel docCatModel = new DoccatModel(new FileInputStream(model));
        myCategorizer = new DocumentCategorizerME(docCatModel);
    }

    @Override
    public String categorize(String text) {
        double[] outcomes = myCategorizer.categorize(text.split(" "));
        String category = myCategorizer.getBestCategory(outcomes);
        //System.out.println(myCategorizer.scoreMap(text.split(" ")));
        return category;
    }
}