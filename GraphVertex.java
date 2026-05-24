package acsse.csc3a.modelgraph;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents one node on the graph.
 * All vertex correspond to one region of the image.
 */
public class GraphVertex {

    // Unique index of the vertex
    private final int index;

    // Row position of the vertex in the 5x5 grid
    private final int row;

    // Column position of the vertex in the 5x5 grid
    private final int col;

    // String ID used for display
    private final String id;

    // Feature vector for this region (brightness, red, green, blue, texture)
    private final double[] features;

    // List of edges connected to this vertex
    private final List<GraphEdge> neighborEdges;

    /**
     * This constructor is responsible to create a new GraphVertex
     * @param index unique index
     * @param row row position in grid
     * @param col column position in grid
     * @param features extracted features from image region
     */
    public GraphVertex(int index, int row, int col, double[] features) {
        this.index = index;
        this.row = row;
        this.col = col;

        // Create a readable ID for display purposes
        this.id = "V(" + row + "," + col + ")";

        this.features = features;

        // Initialize list to store connected edges
        this.neighborEdges = new ArrayList<>();
    }

    /**
     * Returns the index of the vertex
     */
    public int getIndex() {
        return index;
    }

    /**
     * Returns the row position of the vertex
     */
    public int getRow() {
        return row;
    }

    /**
     * Returns the column position of the vertex
     */
    public int getCol() {
        return col;
    }

    /**
     * Returns the ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the feature array for this vertex
     */
    public double[] getFeatures() {
        return features;
    }

    /**
     * Adds an edge to this vertex's list of connected edges
     */
    public void addNeighborEdge(GraphEdge edge) {
        if (edge != null) 
        {
        	neighborEdges.add(edge);
        }
    }

    /**
     * Returns a copy of the list of edges connected to this vertex
     */
    public List<GraphEdge> getIncidentEdges() {
        return new ArrayList<>(neighborEdges);
    }
}