package coal.detection.classifier;

import acsse.detection.datastructures.ArrayListDS;
import acsse.detection.modelgraph.GraphEdge;
import acsse.detection.modelgraph.GraphStructure;
import acsse.detection.modelgraph.GraphVertex;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is a graph neural network which works as follows:
 * Each node reads its neighbours through the graph edges.
 * The updated node values are combined into one graph representation.
 * An output layer gives one score for each coal class.
 */
public class GraphNeuralNetwork {

    // Number of message passing rounds
    private static final int MESSAGE_PASSING_ROUNDS = 2;

    //  Learned output weights for each clas
    private final HashMap<String, double[]> classWeights = new HashMap<>();
    // Each class has one weight vector and one bias value
    private final HashMap<String, Double> classBiases = new HashMap<>();

    // Train the output layer using the pooled graph vectors from the training set.
	public void train(ArrayListDS<TrainingSample> samples) {
    classWeights.clear();
    classBiases.clear();

    if (samples == null || samples.isEmpty())
    {
        return;
    }

    // Find the size of one pooled graph vector
    int vectorSize = 0;
    for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++)
    {
        TrainingSample sample = samples.get(sampleIndex);
        if (sample.getGraph() != null)
        {
            vectorSize = buildGraphVector(sample.getGraph()).length;
            break;
        }
    }

    if (vectorSize == 0)
    {
        return;
    }

    // Create one output neuron for each class label
    for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++)
    {
        TrainingSample sample = samples.get(sampleIndex);
        if (!classWeights.containsKey(sample.getLabel()))
        {
            classWeights.put(sample.getLabel(), new double[vectorSize]);
            classBiases.put(sample.getLabel(), 0.0);
        }
    }

    // Learning rounds
    double learningRate = 0.35;
	// Number of times the model goes through the whole training dataset during training
    int count = 220;

    for (int i = 0; i < count; i++) {
        for (int sampleIndex = 0; sampleIndex < samples.size(); sampleIndex++)
        {
            TrainingSample sample = samples.get(sampleIndex);
            if (sample.getGraph() == null)
            {
                continue;
            }

            double[] pooledGraphVector = buildGraphVector(sample.getGraph());
            HashMap<String, Double> probabilities = predictProbabilities(pooledGraphVector);

            for (String label : classWeights.keySet())
            {
                double expected = label.equals(sample.getLabel()) ? 1.0 : 0.0;
                double predicted = probabilities.get(label);
                double error = expected - predicted;

                double[] weights = classWeights.get(label);
                for (int j = 0; j < weights.length; j++) {
                    weights[j] += learningRate * error * pooledGraphVector[j];
                }
                classBiases.put(label, classBiases.get(label) + learningRate * error);
            }
        }
    }
}

    /**
     * This method predict probabilities for one image graph
     */
    public HashMap<String, Double> classify(GraphStructure graph) {
        if (graph == null || classWeights.isEmpty()) 
        {
            return new HashMap<>();
        }

        double[] pooledGraphVector = buildGraphVector(graph);
        return predictProbabilities(pooledGraphVector);
    }

    /**
     * This method convert a graph into one graph level feature vector
     */
    public double[] buildGraphVector(GraphStructure graph) {
        if (graph == null || graph.vertexCount() == 0) 
        {
            return new double[0];
        }

        int featureCount = graph.getVertices().get(0).getFeatures().length;
        double[][] currentNodeFeatures = new double[graph.vertexCount()][featureCount];

        // Start with the original patch features
        for (GraphVertex vertex : graph.getVertices()) 
        {
            double[] source = vertex.getFeatures();
            currentNodeFeatures[vertex.getIndex()] = copyVector(source);
        }

        // Message passing
        for (int round = 0; round < MESSAGE_PASSING_ROUNDS; round++)
        {
            double[][] nextNodeFeatures = new double[graph.vertexCount()][featureCount];

            for (GraphVertex vertex : graph.getVertices()) 
            {
                double[] ownFeatures = currentNodeFeatures[vertex.getIndex()];
                double[] neighbourMessage = new double[featureCount];
                double totalConnectionStrength = 0.0;

                for (GraphEdge edge : vertex.getIncidentEdges()) 
                {
                    GraphVertex neighbour = edge.getOpposite(vertex);
                    if (neighbour == null) 
					{
                        continue;
                    }

                    // When edge weight are smaller it means the regions are more alike
					// and convert that to connection strength so that strong neighbours can help more
                    double connectionStrength = 1.0 / (1.0 + edge.getWeight());
                    totalConnectionStrength += connectionStrength;

                    double[] neighbourFeatures = currentNodeFeatures[neighbour.getIndex()];
                    for (int i = 0; i < featureCount; i++) {
                        neighbourMessage[i] += connectionStrength * neighbourFeatures[i];
                    }
                }

                if (totalConnectionStrength > 0.0) 
                {
                    for (int i = 0; i < featureCount; i++)
                    {
                        neighbourMessage[i] /= totalConnectionStrength;
                    }
                }

                // Update the node by combining its own information with neighbour information
                double[] updated = new double[featureCount];
                for (int i = 0; i < featureCount; i++)
				{
                    double mixedValue = 0.60 * ownFeatures[i] + 0.40 * neighbourMessage[i];
					// ReLU step
                    updated[i] = Math.max(0.0, mixedValue);
                }
                nextNodeFeatures[vertex.getIndex()] = updated;
            }

            currentNodeFeatures = nextNodeFeatures;
        }

        // Combine all node values into one graph vector
        double[] pooledGraphVector = new double[featureCount + 4];

        for (GraphVertex vertex : graph.getVertices()) 
        {
            double[] nodeFeatures = currentNodeFeatures[vertex.getIndex()];

            for (int i = 0; i < featureCount; i++) 
            {
                pooledGraphVector[i] += nodeFeatures[i];
            }

            pooledGraphVector[featureCount] += vertex.getIncidentEdges().size();
            pooledGraphVector[featureCount + 1] += vertex.getFeatures()[0];
            pooledGraphVector[featureCount + 2] += vertex.getFeatures()[4];
            pooledGraphVector[featureCount + 3] += vertex.getFeatures()[5];
        }

        for (int i = 0; i < pooledGraphVector.length; i++)
        {
            pooledGraphVector[i] /= graph.vertexCount();
        }

        // Replace the last helper value with the average graph edge weight for display and learning
        pooledGraphVector[featureCount + 3] = graph.averageEdgeWeight();
        return pooledGraphVector;
    }

    /**
     * This method score one pooled graph vector against the learned output layer
     */
    private HashMap<String, Double> predictProbabilities(double[] pooledGraphVector) {
        HashMap<String, Double> rawScores = new HashMap<>();
        double maxRawScore = Double.NEGATIVE_INFINITY;

        for (Map.Entry<String, double[]> entry : classWeights.entrySet()) 
        {
            String label = entry.getKey();
            double[] weights = entry.getValue();
            double rawScore = classBiases.get(label);

            for (int i = 0; i < weights.length; i++) {
                rawScore += weights[i] * pooledGraphVector[i];
            }

            rawScores.put(label, rawScore);
            if (rawScore > maxRawScore) 
            {
                maxRawScore = rawScore;
            }
        }

        // This method is a softmax which turns raw scores into probabilities that add up to 1
        HashMap<String, Double> probabilities = new HashMap<>();
        double sum = 0.0;

        for (String label : rawScores.keySet())
        {
            double expScore = Math.exp(rawScores.get(label) - maxRawScore);
            probabilities.put(label, expScore);
            sum += expScore;
        }

        if (sum == 0.0) {
            return probabilities;
        }

        for (String label : probabilities.keySet()) 
        {
            probabilities.put(label, probabilities.get(label) / sum);
        }

        return probabilities;
    }

    /**
     * Create a new array and copy all values from the source array
     * @param source the source array to copy
     * @return copy
     */
    private double[] copyVector(double[] source) 
    {
    	double[] copy = new double[source.length];

    	// Copy each element one by one
    	for (int i = 0; i < source.length; i++) {
    	    copy[i] = source[i];
    	}

    	return copy;
    }
}
