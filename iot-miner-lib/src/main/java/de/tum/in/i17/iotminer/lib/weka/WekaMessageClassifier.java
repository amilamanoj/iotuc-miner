package de.tum.in.i17.iotminer.lib.weka;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToWordVector;

import java.io.File;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;

public class WekaMessageClassifier {
    /**
     * for serialization.
     */
    private static final long serialVersionUID = -123455813150452885L;

    /**
     * The training data gathered so far.
     */
    private Instances m_Data = null;

    /**
     * The filter used to generate the word counts.
     */
    private StringToWordVector m_Filter = new StringToWordVector();

    /**
     * The actual classifier.
     */
    private Classifier m_Classifier = new J48();

    /**
     * Whether the model is up to date.
     */
    private boolean m_UpToDate;

    /**
     * Constructs empty training dataset.
     */
    public WekaMessageClassifier(File dataDir) throws URISyntaxException {
        String nameOfDataset = "MessageClassificationProblem";
        // Create vector of attributes.
        ArrayList<Attribute> attributes = new ArrayList<Attribute>(2);
        // Add attribute for holding messages.
        attributes.add(new Attribute("Message", (ArrayList<String>) null));
        // Add class attribute.
        ArrayList<String> classValues = new ArrayList<String>(2);

        for (File file : dataDir.listFiles()) {
            String fileName = file.getName();
            String className = fileName.split("-")[1];
            className = className.substring(0, className.length() - 4);
            classValues.add(className);

        }
        attributes.add(new Attribute("Class", classValues));
        // Create dataset with initial capacity of 100, and set index of class.
        m_Data = new Instances(nameOfDataset, attributes, 100);
        m_Data.setClassIndex(m_Data.numAttributes() - 1);
    }

    /**
     * Updates model using the given training message.
     *
     * @param message    the    message content
     * @param classValue the class label
     */
    public void updateData(String message, String classValue) {
        // Make message into instance.
        Instance instance = makeInstance(message, m_Data);
        // Set class value for instance.
        instance.setClassValue(classValue);
        // Add instance to training data.
        m_Data.add(instance);
        m_UpToDate = false;
    }

    /**
     * Classifies a given message.
     *
     * @param message the message content
     * @throws Exception if classification fails
     */
    public String classifyMessage(String message) throws Exception {
        // Check whether classifier has been built.
        if (m_Data.numInstances() == 0)
            throw new Exception("No classifier available.");
        // Check whether classifier and filter are up to date.
        if (!m_UpToDate) {
            // Initialize filter and tell it about the input format.
            m_Filter.setInputFormat(m_Data);
            // Generate word counts from the training data.
            Instances filteredData = Filter.useFilter(m_Data, m_Filter);
            // Rebuild classifier.
            System.out.println("Building classifier");
            m_Classifier.buildClassifier(filteredData);
            m_UpToDate = true;
        }
        // Make separate little test set so that message
        // does not get added to string attribute in m_Data.
        Instances testset = m_Data.stringFreeStructure();
        // Make message into test instance.
        Instance instance = makeInstance(message, testset);
        // Filter instance.
        m_Filter.input(instance);
        Instance filteredInstance = m_Filter.output();
        System.out.println("Classifying: " + message);
        // Get index of predicted class value.
        double predicted = m_Classifier.classifyInstance(filteredInstance);
        System.out.println(Arrays.toString(m_Classifier.distributionForInstance(filteredInstance)));
        // Output class value.
        String res = m_Data.classAttribute().value((int) predicted);
        System.out.println("Message classified as : " + res);
        return res;
    }

    /**
     * Method that converts a text message into an instance.
     *
     * @param text the message content to convert
     * @param data the header information
     * @returnthe generated Instance
     */
    private Instance makeInstance(String text, Instances data) {
        // Create instance of length two.
        Instance instance = new DenseInstance(2);
        // Set value for message attribute
        Attribute messageAtt = data.attribute("Message");
        instance.setValue(messageAtt, messageAtt.addStringValue(text));
        // Give instance access to attribute information from the dataset.
        instance.setDataset(data);
        return instance;
    }

}
