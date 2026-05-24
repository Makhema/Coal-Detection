package acsse.csc3a.image;

import acsse.csc3a.modelgraph.GraphStructure;
import acsse.csc3a.modelgraph.GraphVertex;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * This class converts an image into a graph of patches.
 * Each patch becomes one vertex and edges connect neighbouring patches.
 * It also smooths the image with a Gaussian kernel before extracting features so that blurry image does not affect the result too much
 */
public class ImageFeatureExtractor {
    private static final int GRID_ROWS = 5;
    private static final int GRID_COLS = 5;
    private static final int STANDARD_WIDTH = 250;
    private static final int STANDARD_HEIGHT = 250;

    // 3x3 gaussian  kernel
    private static final int[][] GAUSSIAN_KERNEL = {
        {1, 2, 1},
        {2, 4, 2},
        {1, 2, 1}
    };
    private static final double GAUSSIAN_KERNEL_SUM = 16.0;

    // Blur threshold, smaller values means weaker edges and a blurrier image
    private static final double MIN_SHARPNESS_SCORE = 8.0;

    public GraphStructure buildGraph(BufferedImage source) {
        BufferedImage image = preprocessImage(source);
        GraphStructure graph = new GraphStructure();
        GraphVertex[][] vertices = new GraphVertex[GRID_ROWS][GRID_COLS];

        int patchWidth = image.getWidth() / GRID_COLS;
        int patchHeight = image.getHeight() / GRID_ROWS;

        for (int row = 0; row < GRID_ROWS; row++)
        {
            for (int col = 0; col < GRID_COLS; col++)
            {
                int x = col * patchWidth;
                int y = row * patchHeight;
                double[] patchFeatures = calculatePatchFeatures(image, x, y, patchWidth, patchHeight);
                vertices[row][col] = graph.addVertex(row, col, patchFeatures);
            }
        }

        for (int row = 0; row < GRID_ROWS; row++)
        {
            for (int col = 0; col < GRID_COLS; col++)
            {
                if (col + 1 < GRID_COLS)
                {
                    graph.addEdge(vertices[row][col], vertices[row][col + 1], edgeStrength(vertices[row][col].getFeatures(),
                            vertices[row][col + 1].getFeatures()));
                }
                if (row + 1 < GRID_ROWS)
                {
                    graph.addEdge(vertices[row][col], vertices[row + 1][col], edgeStrength(vertices[row][col].getFeatures(),
                            vertices[row + 1][col].getFeatures()));
                }
            }
        }

        return graph;
    }

    /**
     * This method resize and smooth the image before feature extraction
     */
    public BufferedImage preprocessImage(BufferedImage source) {
        BufferedImage resized = resize(source, STANDARD_WIDTH, STANDARD_HEIGHT);
        return applyGaussianKernel(resized);
    }

    /**
     * This boolean method reject images that are too blurry
     */
    public boolean isBlurry(BufferedImage source) {
        return calculateSharpnessScore(source) < MIN_SHARPNESS_SCORE;
    }

    /**
     * This method measure image sharpness using simple horizontal and vertical differences
     */
    public double calculateSharpnessScore(BufferedImage source) {
        BufferedImage image = resize(source, STANDARD_WIDTH, STANDARD_HEIGHT);
        double[][] gray = toGrayMatrix(image);

        double totalDifference = 0.0;
        int count = 0;

        for (int y = 0; y < image.getHeight() - 1; y++) {
            for (int x = 0; x < image.getWidth() - 1; x++) {
                double horizontal = Math.abs(gray[y][x] - gray[y][x + 1]);
                double vertical = Math.abs(gray[y][x] - gray[y + 1][x]);
                totalDifference += horizontal + vertical;
                count += 2;
            }
        }

        if (count == 0) {
            return 0.0;
        }

        return totalDifference / count;
    }

    private double[] calculatePatchFeatures(BufferedImage image, int startX, int startY, int width, int height) {
        double sumGray = 0.0;
        double sumRed = 0.0;
        double sumGreen = 0.0;
        double sumBlue = 0.0;
        double sumGraySquared = 0.0;

        int darkCount = 0;
        int totalCount = 0;

        for (int y = startY; y < startY + height; y++)
        {
            for (int x = startX; x < startX + width; x++)
            {
                int rgb = image.getRGB(x, y);

                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;

                double gray = (red + green + blue) / 3.0;
                totalCount++;

                if (gray < 200.0) {
                    sumRed += red;
                    sumGreen += green;
                    sumBlue += blue;
                    sumGray += gray;
                    sumGraySquared += gray * gray;
                    darkCount++;
                }
            }
        }

        if (darkCount == 0)
        {
            darkCount = 1;
        }
        if (totalCount == 0)
        {
            totalCount = 1;
        }

        double meanGray = sumGray / darkCount;
        double variance = (sumGraySquared / darkCount) - (meanGray * meanGray);
        variance = Math.max(variance, 0.0);

        double darkRatio = (double) darkCount / totalCount;

        return new double[]{
                meanGray / 255.0,
                (sumRed / darkCount) / 255.0,
                (sumGreen / darkCount) / 255.0,
                (sumBlue / darkCount) / 255.0,
                variance / (255.0 * 255.0),
                darkRatio
        };
    }

    public double[] extractFeaturesFromGraph(GraphStructure graph) {
        int vertexCount = graph.vertexCount();
        if (vertexCount == 0)
        {
            return new double[10];
        }

        int featureCount = graph.getVertices().get(0).getFeatures().length;
        double[] embedding = new double[featureCount + 4];

        for (GraphVertex vertex : graph.getVertices()) {
            double[] own = vertex.getFeatures();
            double[] neighbourAverage = new double[featureCount];
            int degree = vertex.getIncidentEdges().size();

            for (int i = 0; i < degree; i++)
            {
                GraphVertex neighbour = vertex.getIncidentEdges().get(i).getOpposite(vertex);
                double[] nFeatures = neighbour.getFeatures();

                for (int f = 0; f < featureCount; f++)
                {
                    neighbourAverage[f] += nFeatures[f];
                }
            }

            for (int f = 0; f < featureCount; f++)
            {
                if (degree > 0)
                {
                    neighbourAverage[f] /= degree;
                }

                double messagePassedValue = 0.6 * own[f] + 0.4 * neighbourAverage[f];
                embedding[f] += messagePassedValue;
            }

            embedding[featureCount] += degree;
            embedding[featureCount + 1] += own[0];
            embedding[featureCount + 2] += own[4];
            embedding[featureCount + 3] += own[5];
        }

        for (int i = 0; i < embedding.length; i++)
        {
            embedding[i] /= vertexCount;
        }

        embedding[featureCount + 3] = graph.averageEdgeWeight();
        return embedding;
    }

    /**
     * This method measure how different two neighbouring patches are
     */
    public double edgeStrength(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++)
        {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    private BufferedImage applyGaussianKernel(BufferedImage input) {
        BufferedImage output = new BufferedImage(input.getWidth(), input.getHeight(), BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < input.getHeight(); y++) {
            for (int x = 0; x < input.getWidth(); x++) {
                double redSum = 0.0;
                double greenSum = 0.0;
                double blueSum = 0.0;
                double weightSum = 0.0;

                for (int kernelRow = -1; kernelRow <= 1; kernelRow++) {
                    for (int kernelCol = -1; kernelCol <= 1; kernelCol++) {
                        int sampleX = clamp(x + kernelCol, 0, input.getWidth() - 1);
                        int sampleY = clamp(y + kernelRow, 0, input.getHeight() - 1);

                        int rgb = input.getRGB(sampleX, sampleY);
                        int red = (rgb >> 16) & 0xFF;
                        int green = (rgb >> 8) & 0xFF;
                        int blue = rgb & 0xFF;

                        int weight = GAUSSIAN_KERNEL[kernelRow + 1][kernelCol + 1];
                        redSum += weight * red;
                        greenSum += weight * green;
                        blueSum += weight * blue;
                        weightSum += weight;
                    }
                }

                int newRed = clamp((int) Math.round(redSum / Math.max(weightSum, GAUSSIAN_KERNEL_SUM)), 0, 255);
                int newGreen = clamp((int) Math.round(greenSum / Math.max(weightSum, GAUSSIAN_KERNEL_SUM)), 0, 255);
                int newBlue = clamp((int) Math.round(blueSum / Math.max(weightSum, GAUSSIAN_KERNEL_SUM)), 0, 255);

                int newRgb = (newRed << 16) | (newGreen << 8) | newBlue;
                output.setRGB(x, y, newRgb);
            }
        }

        return output;
    }

    private double[][] toGrayMatrix(BufferedImage image) {
        double[][] gray = new double[image.getHeight()][image.getWidth()];

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                int green = (rgb >> 8) & 0xFF;
                int blue = rgb & 0xFF;
                gray[y][x] = (red + green + blue) / 3.0;
            }
        }

        return gray;
    }

    private int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    private BufferedImage resize(BufferedImage input, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(input, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }
}
