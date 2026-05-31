package coal.detection.classifier;

import coal.detection.datastructures.ArrayListDS;
import coal.detection.image.ImageFeatureExtractor;
import coal.detection.modelgraph.GraphStructure;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 *@author - Motsoetsoana Makhema, Pedume Madisa, Mayibongwe Mathonsi, Samuel Chichongue.
 * This class analyses an image, rejects images that are not coal,
 * rejects blurry images and then predicts the coal type.
 */
public class CoalClassifier {

    // Safety margins for coal rejection
    private static final double GRAY_MARGIN = 0.12;
    private static final double VARIANCE_MARGIN = 0.020;
    private static final double DARK_RATIO_MARGIN = 0.16;
    private static final double EDGE_WEIGHT_MARGIN = 0.10;
    private static final double COLOUR_SPREAD_MARGIN = 0.08;

    private final ImageFeatureExtractor extractor;
    private final GraphNeuralNetwork graphNeuralNetwork;
    private final ArrayListDS<TrainingSample> trainingSamples;

    // Learned coal ranges used for rejection
    private double minMeanGray;
    private double maxMeanGray;
    private double minVariance;
    private double maxVariance;
    private double minDarkRatio;
    private double maxDarkRatio;
    private double minEdgeWeight;
    private double maxEdgeWeight;
    private double maxColourSpread;

    private boolean trained;

    public CoalClassifier() {
        extractor = new ImageFeatureExtractor();
        graphNeuralNetwork = new GraphNeuralNetwork();
        trainingSamples = new ArrayListDS<>();
        trained = false;
        resetLearnedCoalRanges();
    }

    /**
     * Train the classifier using the training data.
     */
    public void train(ArrayListDS<TrainingSample> samples) {
        trainingSamples.clear();
        resetLearnedCoalRanges();

        for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++) {
            TrainingSample sample = samples.get(sampleIndex);
            trainingSamples.add(sample);
        }

        computeCoalFeatureRanges();
        graphNeuralNetwork.train(trainingSamples);
        trained = !trainingSamples.isEmpty();
    }

    /**
     * Analyse one uploaded image.
     */
    public AnalysisResult analyse(BufferedImage image) {
        if (!trained) {
            throw new IllegalStateException("Train the model first before classifying images.");
        }

        GraphStructure graph = extractor.buildGraph(image);
        double[] featureVector = graphNeuralNetwork.buildGraphVector(graph);

        // Compare with the training set first and If the image strongly matches known coal samples accept it immediately
        SimilarityResult bestMatch = findBestMatch(featureVector);

        if (bestMatch != null && bestMatch.getScore() >= 0.85) {
            HashMap<String, Double> classScores = scoreClassesFromTrainingSet(featureVector);
            applyCoalTypeRules(featureVector, classScores);
            normaliseScores(classScores);

            ArrayListDS<SimilarityResult> topMatches = findTopMatches(featureVector, 3);
            return new AnalysisResult(image, bestMatch.getLabel(), classScores, graph, featureVector, topMatches);
        }

        // Only reject the image if it does not strongly match known coal samples
        if (!passesSceneLevelCoalCheck(image)) {
            throw new IllegalArgumentException("The uploaded image is not coal.");
        }

        if (extractor.isBlurry(image)) {
            throw new IllegalArgumentException("The uploaded image is too blurry. Please upload a clearer image.");
        }

        if (!looksLikeCoal(featureVector)) {
            throw new IllegalArgumentException("The uploaded image is not coal.");
        }

        // Score the image against the training set using the graph vector
        HashMap<String, Double> classScores = scoreClassesFromTrainingSet(featureVector);
        if (classScores.isEmpty()) {
            throw new IllegalArgumentException("The uploaded image could not be analysed.");
        }

        // Class rules to separate the four coal types better
        applyCoalTypeRules(featureVector, classScores);

        String predicted = null;
        double bestRawScore = Double.NEGATIVE_INFINITY;
        double secondBestRawScore = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, Double> entry : classScores.entrySet()) {
            double score = entry.getValue();

            if (score > bestRawScore) {
                secondBestRawScore = bestRawScore;
                bestRawScore = score;
                predicted = entry.getKey();
            } else if (score > secondBestRawScore) {
                secondBestRawScore = score;
            }
        }

        if (bestRawScore < 0.48) {
            throw new IllegalArgumentException("The uploaded image is not coal.");
        }

        if ((bestRawScore - secondBestRawScore) < 0.03 && !isStrongCoalCandidate(featureVector)) {
            throw new IllegalArgumentException("The uploaded image is not coal.");
        }

        normaliseScores(classScores);

        ArrayListDS<SimilarityResult> topMatches = findTopMatches(featureVector, 3);
        if (topMatches.isEmpty()) {
            throw new IllegalArgumentException("The uploaded image is not coal.");
        }

        return new AnalysisResult(image, predicted, classScores, graph, featureVector, topMatches);
    }

    /**
     * Learn the normal coal feature ranges from the training set.
     */
    private void computeCoalFeatureRanges() {
        if (trainingSamples.isEmpty()) {
            return;
        }

        for (int sampleIndex = 0; sampleIndex < trainingSamples.size(); sampleIndex++) {
            TrainingSample sample = trainingSamples.get(sampleIndex);
            double[] featureVector = sample.getFeatures();

            if (featureVector == null || featureVector.length < 10) {
                continue;
            }

            double meanGray = featureVector[0];
            double variance = featureVector[4];
            double darkRatio = featureVector[5];
            double edgeWeight = featureVector[9];
            double colourSpread = computeColourSpread(featureVector);

            minMeanGray = Math.min(minMeanGray, meanGray);
            maxMeanGray = Math.max(maxMeanGray, meanGray);
            minVariance = Math.min(minVariance, variance);
            maxVariance = Math.max(maxVariance, variance);
            minDarkRatio = Math.min(minDarkRatio, darkRatio);
            maxDarkRatio = Math.max(maxDarkRatio, darkRatio);
            minEdgeWeight = Math.min(minEdgeWeight, edgeWeight);
            maxEdgeWeight = Math.max(maxEdgeWeight, edgeWeight);
            maxColourSpread = Math.max(maxColourSpread, colourSpread);
        }
    }

    /**
     * Check if the graph features still fall inside the learned coal range
     */
    private boolean looksLikeCoal(double[] featureVector) {
        if (featureVector == null || featureVector.length < 10) {
            return false;
        }

        double meanGray = featureVector[0];
        double variance = featureVector[4];
        double darkRatio = featureVector[5];
        double edgeWeight = featureVector[9];
        double colourSpread = computeColourSpread(featureVector);

        if (meanGray < minMeanGray - GRAY_MARGIN || meanGray > maxMeanGray + GRAY_MARGIN) {
            return false;
        }

        if (variance < Math.max(0.0, minVariance - VARIANCE_MARGIN) || variance > maxVariance + VARIANCE_MARGIN) {
            return false;
        }

        if (darkRatio < Math.max(0.0, minDarkRatio - DARK_RATIO_MARGIN)
                || darkRatio > Math.min(1.0, maxDarkRatio + DARK_RATIO_MARGIN)) {
            return false;
        }

        if (edgeWeight < Math.max(0.0, minEdgeWeight - EDGE_WEIGHT_MARGIN)
                || edgeWeight > maxEdgeWeight + EDGE_WEIGHT_MARGIN) {
            return false;
        }

        if (colourSpread > maxColourSpread + COLOUR_SPREAD_MARGIN) {
            return false;
        }

        return true;
    }

    /**
     * Reject images that are clearly not coal
     */
    private boolean passesSceneLevelCoalCheck(BufferedImage image) {
        if (image == null) {
            return false;
        }

        int width = image.getWidth();
        int height = image.getHeight();
        int totalPixels = Math.max(1, width * height);

        int brightBlueCount = 0;
        int warmEarthCount = 0;
        int neutralDarkCount = 0;
        int veryDarkNeutralCount = 0;
        int brightNeutralCount = 0;
        double sumGray = 0.0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                double gray = (red + green + blue) / 3.0 / 255.0;
                sumGray += gray;

                int colourRange = Math.max(red, Math.max(green, blue))
                        - Math.min(red, Math.min(green, blue));
                double brightness255 = (red + green + blue) / 3.0;

                if (blue > red + 25 && blue > green + 20 && blue > 120 && brightness255 > 100) {
                    brightBlueCount++;
                }

                if (red > green && green > blue && (red - blue) > 25 && brightness255 > 80) {
                    warmEarthCount++;
                }

                if (brightness255 < 140 && colourRange < 35) {
                    neutralDarkCount++;
                }

                if (brightness255 < 90 && colourRange < 45) {
                    veryDarkNeutralCount++;
                }

                if (brightness255 > 180 && colourRange < 30) {
                    brightNeutralCount++;
                }
            }
        }

        double brightBlueRatio = (double) brightBlueCount / totalPixels;
        double warmEarthRatio = (double) warmEarthCount / totalPixels;
        double neutralDarkRatio = (double) neutralDarkCount / totalPixels;
        double veryDarkNeutralRatio = (double) veryDarkNeutralCount / totalPixels;
        double brightNeutralRatio = (double) brightNeutralCount / totalPixels;
        double meanGray = sumGray / totalPixels;

        if (neutralDarkRatio < 0.08 && warmEarthRatio > 0.28) {
            return false;
        }

        // Bright blue sky or outdoor scenes are rejected
        if (brightBlueRatio > 0.14 && warmEarthRatio > 0.18) {
            return false;
        }

        // Very bright colourful scenes are rejected
        if (meanGray > 0.78) {
            return false;
        }

        if (brightNeutralRatio > 0.26 && veryDarkNeutralRatio > 0.14) {
            return false;
        }

        if (brightNeutralRatio > 0.12 && brightBlueRatio > 0.05) {
            return false;
        }

        return true;
    }

    /**
     * Find the closest training samples using a feature agreement score
     */
    private ArrayListDS<SimilarityResult> findTopMatches(double[] featureVector, int howMany) {
        ArrayListDS<SimilarityResult> results = new ArrayListDS<>();

        for (int sampleIndex = 0; sampleIndex < trainingSamples.size(); sampleIndex++) {
            TrainingSample sample = trainingSamples.get(sampleIndex);
            double score = computeFeatureAgreement(featureVector, sample.getFeatures());
            SimilarityResult match = new SimilarityResult(sample.getLabel(), sample.getRelativePath(), score);

            int insertIndex = 0;
            while (insertIndex < results.size() && results.get(insertIndex).getScore() >= score) {
                insertIndex++;
            }

            results.add(insertIndex, match);
            if (results.size() > howMany) {
                results.remove(results.size() - 1);
            }
        }

        return results;
    }

    /**
     * Find the best training match
     */
    private SimilarityResult findBestMatch(double[] featureVector) {
        SimilarityResult bestMatch = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int sampleIndex = 0; sampleIndex < trainingSamples.size(); sampleIndex++) {
            TrainingSample sample = trainingSamples.get(sampleIndex);
            double score = computeFeatureAgreement(featureVector, sample.getFeatures());

            if (score > bestScore) {
                bestScore = score;
                bestMatch = new SimilarityResult(sample.getLabel(), sample.getRelativePath(), score);
            }
        }

        return bestMatch;
    }

    /**
     * Give one score per class by checking which training samples agree most with the new graph vector
     */
    private HashMap<String, Double> scoreClassesFromTrainingSet(double[] featureVector) {
        HashMap<String, Double> classScores = new HashMap<>();

        for (int sampleIndex = 0; sampleIndex < trainingSamples.size(); sampleIndex++) {
            TrainingSample sample = trainingSamples.get(sampleIndex);

            if (!classScores.containsKey(sample.getLabel())) {
                classScores.put(sample.getLabel(), 0.0);
            }
        }

        HashMap<String, ArrayListDS<Double>> bestScoresPerClass = new HashMap<>();
        for (String label : classScores.keySet()) {
            bestScoresPerClass.put(label, new ArrayListDS<Double>());
        }

        for (int sampleIndex = 0; sampleIndex < trainingSamples.size(); sampleIndex++) {
            TrainingSample sample = trainingSamples.get(sampleIndex);
            double score = computeFeatureAgreement(featureVector, sample.getFeatures());
            ArrayListDS<Double> currentScores = bestScoresPerClass.get(sample.getLabel());

            int insertIndex = 0;
            while (insertIndex < currentScores.size() && currentScores.get(insertIndex) >= score) {
                insertIndex++;
            }

            currentScores.add(insertIndex, score);
            if (currentScores.size() > 3) {
                currentScores.remove(currentScores.size() - 1);
            }
        }

        for (String label : bestScoresPerClass.keySet()) {
            ArrayListDS<Double> currentScores = bestScoresPerClass.get(label);

            if (currentScores.isEmpty()) {
                classScores.put(label, 0.0);
                continue;
            }

            double sum = 0.0;
            for (int scoreIndex = 0; scoreIndex < currentScores.size(); scoreIndex++) {
                sum += currentScores.get(scoreIndex);
            }

            classScores.put(label, sum / currentScores.size());
        }

        return classScores;
    }

    /**
     * Add class rules on top of the graph scores
     */
    private void applyCoalTypeRules(double[] featureVector, HashMap<String, Double> classScores) {
        double meanGray = featureVector[0];
        double red = featureVector[1];
        double green = featureVector[2];
        double blue = featureVector[3];
        double variance = featureVector[4];
        double darkRatio = featureVector[5];
        double edgeWeight = featureVector[9];

        if(edgeWeight > 0.36 && meanGray < 0.32){
            addScore(classScores, "Anthracite", 0.12);
        }

        if(meanGray > 0.33 && darkRatio > 0.78){
            addScore(classScores, "Peat", 0.14);
        }

        if(variance < 0.015 || edgeWeight < 0.24 || red > blue + 0.03){
            addScore(classScores, "Lignite", 0.10);
        }

        if(meanGray >= 0.22 && meanGray <= 0.33
                && edgeWeight >= 0.24 && edgeWeight <= 0.38
                && darkRatio >= 0.58 && darkRatio <= 0.85) {
            addScore(classScores, "Bituminous", 0.14);
        }

        if(meanGray > 0.35){
            addScore(classScores, "Anthracite", -0.05);
        }

        if(edgeWeight > 0.34 && meanGray < 0.28){
            addScore(classScores, "Peat", -0.05);
        }

        if(green > blue && red > green){
            addScore(classScores, "Lignite", 0.02);
        }
    }

    private void addScore(HashMap<String, Double> classScores, String label, double extraScore) {
        if(classScores.containsKey(label)){
            classScores.put(label, Math.max(0.0, classScores.get(label) + extraScore));
        }
    }

    /**
     * Turn scores into values that add up to 1
     */
    private void normaliseScores(HashMap<String, Double> classScores) {
        double sum = 0.0;

        for (double score : classScores.values()) {
            sum += Math.max(0.0, score);
        }

        if (sum <= 0.0) {
            return;
        }

        for (String label : classScores.keySet()) {
            classScores.put(label, Math.max(0.0, classScores.get(label)) / sum);
        }
    }

    /**
     * Feature agreement score between two graph vectors
     */
    private double computeFeatureAgreement(double[] first, double[] second){
        double[] tolerance = {0.09, 0.10, 0.10, 0.10, 0.015, 0.18, 0.40, 0.09, 0.015, 0.14};
        double[] weight =    {1.20, 0.70, 0.70, 0.70, 1.10, 1.20, 0.00, 0.60, 0.90, 1.10};

        double score = 0.0;
        double weightSum = 0.0;

        for(int i = 0; i < first.length && i < second.length && i < tolerance.length; i++){
            if (weight[i] == 0.0) 
			{
                continue;
            }

            double difference = Math.abs(first[i] - second[i]);
            double contribution = 1.0 - (difference / tolerance[i]);

            if(contribution < 0.0) {
                contribution = 0.0;
            }

            score += contribution * weight[i];
            weightSum += weight[i];
        }

        if(weightSum == 0.0) {
            return 0.0;
        }

        return score / weightSum;
    }

    /**
     * Strong coal images may still pass even if the class margin is small
     */
    private boolean isStrongCoalCandidate(double[] featureVector) {
        double meanGray = featureVector[0];
        double darkRatio = featureVector[5];
        double edgeWeight = featureVector[9];
        return meanGray >= 0.18 && meanGray <= 0.36
                && darkRatio >= 0.50
                && edgeWeight >= 0.10 && edgeWeight <= 0.50;
    }

    private double computeColourSpread(double[] featureVector) {
        double red = featureVector[1];
        double green = featureVector[2];
        double blue = featureVector[3];
        return Math.max(Math.abs(red - green), Math.max(Math.abs(red - blue), Math.abs(green - blue)));
    }

    private void resetLearnedCoalRanges() {
        minMeanGray = Double.POSITIVE_INFINITY;
        maxMeanGray = Double.NEGATIVE_INFINITY;
        minVariance = Double.POSITIVE_INFINITY;
        maxVariance = Double.NEGATIVE_INFINITY;
        minDarkRatio = Double.POSITIVE_INFINITY;
        maxDarkRatio = Double.NEGATIVE_INFINITY;
        minEdgeWeight = Double.POSITIVE_INFINITY;
        maxEdgeWeight = Double.NEGATIVE_INFINITY;
        maxColourSpread = 0.0;
    }
}
