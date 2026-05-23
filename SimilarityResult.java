package acsse.csc3a.classifier;

/**
 * This class holds one similarity match and its score
 */
public class SimilarityResult {
    private final String label;
    private final String sourceImage;
    private final double score;

    public SimilarityResult(String label, String sourceImage, double score) {
        this.label = label;
        this.sourceImage = sourceImage;
        this.score = score;
    }

    public String getLabel() {
        return label;
    }

    public String getSourceImage() {
        return sourceImage;
    }

    public String getRelativePath() {
        return sourceImage;
    }

    public double getScore() {
        return score;
    }

    public double getSimilarity() {
        return score;
    }

    public double getDistance() {
        return 1.0 - score;
    }
}
