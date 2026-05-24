package acsse.csc3a.modelgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the whole graph.
 * It stores all vertices and edges.
 */
public class GraphStructure {

    // List of all vertices in the graph
    private final List<GraphVertex> vertices = new ArrayList<>();

    // List of all edges
    private final List<GraphEdge> edges = new ArrayList<>();

    /**
     * This method creates and adds a new vertex to the graph
     * @param row the row position in the 5x5 grid
     * @param col column position in the 5x5 grid
     * @param features extracted features for this region
     * @return the newly created vertex
     */
    public GraphVertex addVertex(int row, int col, double[] features) {
        GraphVertex vertex = new GraphVertex(vertices.size(), row, col, features);
        vertices.add(vertex);
        return vertex;
    }

    /**
     * This method creates and adds an edge between two vertices
     * @param from starting vertex
     * @param to ending vertex
     * @param weight similarity or distance between the two regions
     * @return the created edge, or null if invalid
     */
    public GraphEdge addEdge(GraphVertex from, GraphVertex to, double weight) {

        // Does not allow null vertices or region to connect to itself
        if (from == null || to == null || from == to) {
            return null;
        }

        // Does not allow duplicate edges
        if (findEdge(from, to) != null) 
        {
            return null;
        }

        // Create new edge
        GraphEdge edge = new GraphEdge(from, to, weight);

        // Add edge to graph edge list
        edges.add(edge);

        // Add edge to both vertices (undirected graph)
        from.addNeighborEdge(edge);
        to.addNeighborEdge(edge);

        return edge;
    }

    /**
     * Finds an edge between two vertices
     */
    public GraphEdge findEdge(GraphVertex first, GraphVertex second) {
    	 // Loop through all edges in the graph
        for(GraphEdge edge : edges) 
        {
        	// Check if this edge connects the two vertices in both directions
            if ((edge.getFrom() == first && edge.getTo() == second) || (edge.getFrom() == second && edge.getTo() == first)) 
            {
                return edge; // Edge found
            }
        }

        return null; // No edge found
    }

    /**
     * Returns a copy of the list of vertices
     */
    public List<GraphVertex> getVertices() {
        return new ArrayList<>(vertices);
    }
    
    /**
     * Returns a copy of all edges in the graph
     */
    public List<GraphEdge> getEdges() {
        return new ArrayList<>(edges);
    }

    /**
     * Returns number of vertices in the graph
     */
    public int vertexCount() {
        return vertices.size();
    }

    /**
     * Returns number of edges in the graph
     */
    public int edgeCount() {
        return edges.size();
    }

    /**
     * Returns the number of vertices in the graph
     */
    public int getVertexCount() {
        return vertexCount();
    }

    /**
     * Returns the number of edges in the graph
     */
    public int getEdgeCount() {
        return edgeCount();
    }

    /**
     * Calculates the average weight of all edges which is used to understand similarity between regions
     */
    public double averageEdgeWeight() {
        if (edges.isEmpty()) 
        {
            return 0.0;
        }

        double sum = 0.0;

        for(GraphEdge edge : edges) 
        {
        	// Sum all edge weights
            sum += edge.getWeight();
        }

        // Return average
        return sum / edges.size();
    }

    /**
     * Returns the average weight of all edges in the graph
     */
    public double getAverageEdgeWeight() {
        return averageEdgeWeight();
    }

    /**
     * Calculates edge density of the graph
     */
    public double getEdgeDensity() {

        int vertexCount = vertices.size();

        // If less than 2 vertices no edges possible
        if (vertexCount < 2) 
        {
            return 0.0;
        }

        // Maximum possible edges in an undirected graph
        double possibleEdges = (vertexCount * (vertexCount - 1)) / 2.0;

        // Density ratio
        return edges.size() / possibleEdges;
    }
}