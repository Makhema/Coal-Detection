package coal.detection.graph;

import coal.detection.classifier.AnalysisResult;
import coal.detection.modelgraph.GraphEdge;
import coal.detection.modelgraph.GraphStructure;
import coal.detection.modelgraph.GraphVertex;
import coal.detection.app.AppState;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

/**
 * This class is responsible for the Graph pane.
 * The graph is built from a 5 x 5 image grid with 25 nodes and 40 edges
 */
public class Graph extends VBox {
    // Main heading
    private final Label header = new Label("Graph");

    // Short explanation below the heading
    private final Label intro = new Label("No graph has been built yet. Upload and analyse an image first.");

    // Grid used for the text summary under the visual graph
    private final GridPane gp = new GridPane();

    // Scroll pane used for displaying all nodes and edges
    private final ScrollPane scrollPane = new ScrollPane(gp);

    // Canvas used to draw the Graph Representation of Image
    private final Canvas graphCanvas = new Canvas(520, 320);

    // Used to draw shapes and lines on the graph canvas
    private final GraphicsContext graphGc = graphCanvas.getGraphicsContext2D();

    public Graph() {
        setSpacing(15);
        setPadding(new Insets(20));

        header.setStyle("-fx-font-size: 30; -fx-font-weight: bold");
        intro.setWrapText(true);

        gp.setHgap(15);
        gp.setVgap(10);

        // Make the canvas easy to see
        graphCanvas.setStyle("-fx-border-color: lightgrey; -fx-border-width: 1;");
        drawEmptyCanvasMessage();

        scrollPane.setFitToWidth(true);

        this.getChildren().addAll(header, intro, graphCanvas, scrollPane);
    }

    /**
     * Refresh the pane from the latest analysed results
     */
    public void refresh() {
        gp.getChildren().clear();
        AnalysisResult result = AppState.getInstance().getLatestResult();

        // Show this message if image has been analysed
        if (result == null) 
        {
            intro.setText("No graph has been built yet. You need to upload and analyse an image first.");
            drawEmptyCanvasMessage();
            return;
        }

        GraphStructure graph = result.getGraph();
        intro.setText("The uploaded image is converted into a 5 x 5 region graph. "
                + "Each node stores average colour and intensity information for one region.\r\n"
        		+ "Image Graph View");

        // Draw the graph
        drawGraph(graph);

        int row = 0;
        gp.add(new Label("Predicted Label:"), 0, row);
        gp.add(new Label(result.getPredictedLabel()), 1, row++);

        gp.add(new Label("Node Count:"), 0, row);
        gp.add(new Label(String.valueOf(graph.getVertexCount())), 1, row++);

        gp.add(new Label("Edge Count:"), 0, row);
        gp.add(new Label(String.valueOf(graph.getEdgeCount())), 1, row++);

        gp.add(new Label("Average Edge Weight:"), 0, row);
        gp.add(new Label(String.format("%.4f", graph.getAverageEdgeWeight())), 1, row++);

        row++;
        gp.add(new Label("All Nodes:"), 0, row++);

        // Show all nodes
        for (GraphVertex vertex : graph.getVertices()) 
        {
            gp.add(new Label("Node (" + vertex.getRow() + ", " + vertex.getCol() + ")"), 0, row);
            gp.add(new Label(formatFeatureText(vertex.getFeatures())), 1, row++);
        }

        row++;
        gp.add(new Label("All Edges:"), 0, row++);
        
        // Show all edges
        for (GraphEdge edge : graph.getEdges()) 
        {
            gp.add(new Label(edge.getFrom().getId().replace("V", "Vertex") + " to " + edge.getTo().getId().replace("V", "Vertex")), 0, row);
            gp.add(new Label(String.format("weight=%.4f", edge.getWeight())), 1, row++);
        }
    }

    /**
     * Converts the numeric feature array into readable text
     */
    private String formatFeatureText(double[] features) {
        return String.format("brightness=%.3f, red=%.3f, green=%.3f, blue=%.3f, texture=%.3f",
        		features[0], features[1], features[2], features[3], features[4]);
    }

    /**
     * Draw a placeholder message when no graph is available
     */
    private void drawEmptyCanvasMessage() {
        graphGc.clearRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());
        graphGc.setFill(Color.WHITE);
        graphGc.fillRect(0, 0, graphCanvas.getWidth(), graphCanvas.getHeight());
        graphGc.setFill(Color.GRAY);
        graphGc.fillText("Analyse an image first to display the graph here.", 150, 160);
    }

    /**
     * Draw the actual graph on the canvas
     */
    private void drawGraph(GraphStructure graph) {
        double canvasWidth = graphCanvas.getWidth();
        double canvasHeight = graphCanvas.getHeight();

        // Clear old drawing
        graphGc.clearRect(0, 0, canvasWidth, canvasHeight);
        graphGc.setFill(Color.WHITE);
        graphGc.fillRect(0, 0, canvasWidth, canvasHeight);

        // This values control where the graph sits on the page
        double startX = 70;
        double startY = 50;
        double gapX = 95;
        double gapY = 55;
        double radius = 12;

        // Draw all edges
        graphGc.setStroke(Color.DARKGRAY);
        for (GraphEdge edge : graph.getEdges()) 
        {
            GraphVertex from = edge.getFrom();
            GraphVertex to = edge.getTo();

            double x1 = startX + from.getCol() * gapX;
            double y1 = startY + from.getRow() * gapY;
            double x2 = startX + to.getCol() * gapX;
            double y2 = startY + to.getRow() * gapY;

            graphGc.strokeLine(x1, y1, x2, y2);
        }

        // Draw each node as a small circle
        for (GraphVertex vertex : graph.getVertices()) 
        {
            double x = startX + vertex.getCol() * gapX;
            double y = startY + vertex.getRow() * gapY;

            // Use brightness to choose a simple gray colour for the node
            double brightness = vertex.getFeatures()[0];
            brightness = Math.max(0.0, Math.min(1.0, brightness));
            Color nodeColor = Color.gray(brightness);

            graphGc.setFill(nodeColor);
            graphGc.fillOval(x - radius, y - radius, radius * 2, radius * 2);

            graphGc.setStroke(Color.BLACK);
            graphGc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}
