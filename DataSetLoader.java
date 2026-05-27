package coal.detection.service;

import coal.detection.classifier.TrainingSample;
import coal.detection.datastructures.ArrayListDS;
import coal.detection.image.ImageFeatureExtractor;
import coal.detection.modelgraph.GraphStructure;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * This class loads training images directly from folders in the data directory.
 */
public class DataSetLoader {

    private final ImageFeatureExtractor extractor = new ImageFeatureExtractor();
    private final File dataFolder;

    public DataSetLoader() {
        dataFolder = new File("data");
    }

    public ArrayListDS<TrainingSample> loadTrainingSamples() throws IOException {
        ArrayListDS<TrainingSample> samples = new ArrayListDS<>();
        File trainingFolder = new File(dataFolder, "training");

        if (!trainingFolder.exists() || !trainingFolder.isDirectory()) 
		{
            throw new IOException("Training folder not found: " + trainingFolder.getPath());
        }

        File[] allFiles = trainingFolder.listFiles();

        if (allFiles == null) 
		{
            throw new IOException("Could not read training folders.");
        }

        sortFilesByName(allFiles);

        for (int i = 0; i < allFiles.length; i++) 
		{
            File labelFolder = allFiles[i];

            // Only use folders for class labels
            if (!labelFolder.isDirectory()) 
			{
                continue;
            }

            String label = labelFolder.getName();
            File[] imageFiles = labelFolder.listFiles();

            if (imageFiles == null) 
			{
                continue;
            }

            sortFilesByName(imageFiles);

            for (int j = 0; j < imageFiles.length; j++) 
			{
                File imageFile = imageFiles[j];

                // Only use supported image files
                if (!imageFile.isFile()) {
                    continue;
                }

                if (!isSupportedImageFile(imageFile.getName())) {
                    continue;
                }

                BufferedImage image = readImage(imageFile);
                GraphStructure graph = extractor.buildGraph(image);
                double[] featureVector = extractor.extractFeaturesFromGraph(graph);

                String relativePath = label + "/" + imageFile.getName();

                samples.add(new TrainingSample(label, relativePath, image, graph, featureVector));
            }
        }

        return samples;
    }

    public BufferedImage readImage(File file) throws IOException {
        if (!file.exists()) 
		{
            throw new IOException("Image file not found: " + file.getPath());
        }

        BufferedImage image = ImageIO.read(file);

        if (image == null) 
		{
            throw new IOException("Unsupported image file: " + file.getPath());
        }

        return image;
    }

    private boolean isSupportedImageFile(String fileName) {
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".png");
    }

    // Sort files by name
    private void sortFilesByName(File[] files) {
        for (int i = 0; i < files.length - 1; i++) 
		{
            for (int j = 0; j < files.length - 1 - i; j++) 
			{
                if (files[j].getName().compareTo(files[j + 1].getName()) > 0) 
				{
                    File temp = files[j];
                    files[j] = files[j + 1];
                    files[j + 1] = temp;
                }
            }
        }
    }
}