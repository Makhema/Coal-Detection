package coal.detection.classifier;

import coal.detection.modelgraph.GraphStructure;

import java.awt.image.BufferedImage;

/**
 * This class represents one labelled image used during training
 */
public class TrainingSample {

    // Label of the sample
    private final String label;

    // Path to the image inside the dataset
    private final String dataPath;

    // The actual image
    private final BufferedImage image;

    // Graph built from the image
    private final GraphStructure graph;

    // Graph-level feature vector extracted from the image
    private final double[] featureVector;

    /**
     * This constructor to create a training sample
     */
    public TrainingSample(String label, String dataPath, BufferedImage image, GraphStructure graph, double[] featureVector) {
        this.label = label;
        this.dataPath = dataPath;
        this.image = image;
        this.graph = graph;
        this.featureVector = featureVector;
    }

    public String getLabel() {
        return label;
    }

    public String getDataPath() {
        return dataPath;
    }

    public String getRelativePath() {
        return dataPath;
    }

    public BufferedImage getImage() {
        return image;
    }

    public GraphStructure getGraph() {
        return graph;
    }

    public double[] getFeatures() {
        return featureVector;
    }
}
