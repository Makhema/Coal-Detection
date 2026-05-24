package acsse.csc3a.service;

import acsse.csc3a.classifier.AnalysisResult;
import acsse.csc3a.classifier.CoalClassifier;
import acsse.csc3a.classifier.TrainingSample;
import acsse.csc3a.datastructures.ArrayListDS;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class loads the training dataset, trains the classifier and analyses images uploaded by the user.
 */
public class CoalAnalysisService {

    // Loads training images from the data folder
    private final DataSetLoader loader;

    // Performs training and classification of images
    private final CoalClassifier classifier;

    /**
     * This constructor creates the loader and classifier objects.
     */
    public CoalAnalysisService() {
        loader = new DataSetLoader();
        classifier = new CoalClassifier();
    }

    /**
     * This method loads training data and trains the classifier.
     */
    public void initialise() throws IOException {

        // Load training samples from the dataset
        ArrayListDS<TrainingSample> trainingSamples = loader.loadTrainingSamples();

        // Train the classifier using the training data
        classifier.train(trainingSamples);
    }

    /**
     * This class analyses an external image selected by the user.
     * @param file the image file chosen by the user
     * @return AnalysisResult containing classification and graph data
     */
    public AnalysisResult analyseExternalImage(File file) throws IOException {

        // Read image from file
        BufferedImage image = ImageIO.read(file);

        // Check if file is a valid image
        if (image == null) {
            throw new IOException("The selected file is not a supported image.");
        }

        // Analyse the image
        return classifier.analyse(image);
    }

    /**
     * Analyses an already loaded image.
     * @param image BufferedImage object
     * @return AnalysisResult containing classification and graph data
     */
    public AnalysisResult analyseBufferedImage(BufferedImage image) {

        // Directly analyse the given image
        return classifier.analyse(image);
    }
}
