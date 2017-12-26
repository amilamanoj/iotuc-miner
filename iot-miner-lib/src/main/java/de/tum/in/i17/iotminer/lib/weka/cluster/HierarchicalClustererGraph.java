package de.tum.in.i17.iotminer.lib.weka.cluster;
import java.awt.Container;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JFrame;

import weka.clusterers.HierarchicalClusterer;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.EuclideanDistance;
import weka.core.Instances;
import weka.gui.hierarchyvisualizer.HierarchyVisualizer;


public class HierarchicalClustererGraph {

    static HierarchicalClusterer clusterer;
    static Instances data;

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Instantiate clusterer
        clusterer = new HierarchicalClusterer();
        clusterer.setOptions(new String[] {"-L", "COMPLETE"});
        clusterer.setDebug(true);
        clusterer.setNumClusters(2);
        clusterer.setDistanceFunction(new EuclideanDistance());
        clusterer.setDistanceIsBranchLength(true);

        // Build dataset
        ArrayList<Attribute> attributes = new ArrayList<Attribute>();
        attributes.add(new Attribute("A"));
        attributes.add(new Attribute("B"));
        attributes.add(new Attribute("C"));
        data = new Instances("Weka test", attributes, 3);

        // Add data
        data.add(new DenseInstance(1.0, new double[] { 1.0, 0.0, 1.0 }));
        data.add(new DenseInstance(1.0, new double[] { 0.5, 0.0, 1.0 }));
        data.add(new DenseInstance(1.0, new double[] { 0.0, 1.0, 0.0 }));
        data.add(new DenseInstance(1.0, new double[] { 0.0, 1.0, 0.3 }));

        // Cluster network
        clusterer.buildClusterer(data);

        // Print normal
        clusterer.setPrintNewick(false);
        System.out.println("G " + clusterer.graph());
        // Print Newick
        clusterer.setPrintNewick(true);
        System.out.println("G " + clusterer.graph());

        // Let's try to show this clustered data!
        JFrame mainFrame = new JFrame("Weka Test");
        mainFrame.setSize(600, 400);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Container content = mainFrame.getContentPane();
        content.setLayout(new GridLayout(1, 1));

        HierarchyVisualizer visualizer = new HierarchyVisualizer(clusterer.graph());
        content.add(visualizer);

        mainFrame.setVisible(true);
    }

}