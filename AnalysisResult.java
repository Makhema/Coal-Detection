package coal.detection.classifier;

import coal.detection.datastructures.ArrayListDS;
import coal.detection.modelgraph.GraphStructure;

import java.awt.image.BufferedImage;
import java.util.Map;

/**
 * This class is responsible for the final results returned after an image is analysed.
 */
public class AnalysisResult {
    private final BufferedImage analysedImage;
    private final String predictedLabel;
    private final Map<String, Double> classScores;
    private final GraphStructure graph;
    private final double[] embedding;
    private final ArrayListDS<SimilarityResult> topMatches;

    public AnalysisResult(BufferedImage analysedImage, String predictedLabel, Map<String, Double> classScores,GraphStructure graph,
    		double[] embedding, ArrayListDS<SimilarityResult> topMatches) 
    {
        this.analysedImage = analysedImage;
        this.predictedLabel = predictedLabel;
        this.classScores = classScores;
        this.graph = graph;
        this.embedding = embedding;
        this.topMatches = topMatches;
    }

    public BufferedImage getAnalysedImage() {
        return analysedImage;
    }

    public String getPredictedLabel() {
        return predictedLabel;
    }

    public Map<String, Double> getClassScores() {
        return classScores;
    }

    public GraphStructure getGraph() {
        return graph;
    }

    public double[] getEmbedding() {
        return embedding;
    }

    public ArrayListDS<SimilarityResult> getTopMatches() {
        return topMatches;
    }

    public double getConfidence() {
        if(classScores == null || classScores.isEmpty() || predictedLabel == null)
        {
            return 0.0;
        }
        double total = 0.0;
        for(double score : classScores.values()) 
        {
            total += Math.max(0.0, score);
        }
        if(total <= 0.0) 
		{
            return 0.0;
        }
        Double best = classScores.get(predictedLabel);
        return best == null ? 0.0 : Math.max(0.0, best) / total;
    }

    public SimilarityResult getBestMatch() 
    {
        return topMatches != null && !topMatches.isEmpty() ? topMatches.get(0) : null;
    }

    public String getAverageNodeColourDescription() {
        double avgRed = embedding.length > 1 ? embedding[1] * 255.0 : 0.0;
        double avgGreen = embedding.length > 2 ? embedding[2] * 255.0 : 0.0;
        double avgBlue = embedding.length > 3 ? embedding[3] * 255.0 : 0.0;
        double brightness = embedding.length > 0 ? embedding[0] : 0.0;

        if(brightness < 0.18) 
        {
            return "Very dark grey";
        }
        if(avgRed > avgBlue + 18 && avgRed > avgGreen + 12) 
        {
            return "Warm brown grey";
        }
        if(avgBlue > avgRed + 12)
        {
            return "Cool blue grey";
        }
        if(brightness < 0.35) 
        {
            return "Dark grey";
        }
        if(brightness < 0.55) 
        {
            return "Medium grey";
        }
        return "Light grey";
    }

    public String getEdgeDensityDescription() {
        double density = graph.getEdgeDensity();
        if(density < 0.12) 
        {
            return "Low";
        }
        if(density < 0.20)
        {
            return "Moderate";
        }
        return "High";
    }
}
