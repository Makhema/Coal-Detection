package acsse.csc3a.modelgraph;

/**
 * One weighted graph edge between neighbouring image regions.
 */
public class GraphEdge {
	// The first vertex of the edge
    private final GraphVertex from;

    // The second vertex of the edge
    private final GraphVertex to;

    // Weight of the edge which represents similarity or difference between regions
    private final double weight;

    /**
     * This constructor to create a new edge between two vertices
     * @param from starting vertex
     * @param to ending vertex
     * @param weight value representing connection strength
     */
    public GraphEdge(GraphVertex from, GraphVertex to, double weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }
    
    /**
     * Returns the starting vertex of the edge
     */
    public GraphVertex getFrom() {
        return from;
    }

    /**
     * Returns the ending vertex of the edge
     */
    public GraphVertex getTo() {
        return to;
    }

    /**
     * Returns the weight of the edge
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Given one vertex returns the vertex on the opposite side of the edge.
     * @param vertex the given vertex
     * @return the opposite vertex or null if the vertex is not part of this edge
     */
    public GraphVertex getOpposite(GraphVertex vertex) {

        if (vertex == from) 
        {
            return to;
        }

        if (vertex == to) 
        {
            return from;
        }

        return null;
    }
}
