package acsse.csc3a.results;

import acsse.csc3a.classifier.AnalysisResult;
import acsse.csc3a.classifier.SimilarityResult;
import acsse.csc3a.app.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

// This class shows the prediction, confidence and graph details
public class Results extends VBox {

    Label header = new Label("Results");
    Label qualityLbl = new Label("Coal Quality");
    Label confidenceLbl = new Label("Confidence:");
    Label graphSmlryLbl = new Label("Graph Score:");
    Label nodeColorLbl = new Label("Average Node Colour:");
    Label edgeLbl = new Label("Edge Density:");
    Label bestMatchLbl = new Label("Best Match:");
    Label statusLbl = new Label("No analysis has been completed yet.");

    GridPane gpane = new GridPane();

    private final Label qualityValue = new Label("-");
    private final Label confidenceValue = new Label("-");
    private final Label graphSimilarityValue = new Label("-");
    private final Label nodeColorValue = new Label("-");
    private final Label edgeValue = new Label("-");
    private final Label bestMatchValue = new Label("-");

    // Create the labels shown in the results pane
    public Results() {
        setSpacing(15);
        setPadding(new Insets(20));

        gpane.add(qualityLbl, 0, 0);
        gpane.add(qualityValue, 1, 0);
        gpane.add(confidenceLbl, 0, 1);
        gpane.add(confidenceValue, 1, 1);
        gpane.add(graphSmlryLbl, 0, 2);
        gpane.add(graphSimilarityValue, 1, 2);
        gpane.add(nodeColorLbl, 0, 3);
        gpane.add(nodeColorValue, 1, 3);
        gpane.add(edgeLbl, 0, 4);
        gpane.add(edgeValue, 1, 4);
        gpane.add(bestMatchLbl, 0, 5);
        gpane.add(bestMatchValue, 1, 5);

        header.setStyle("-fx-font-size: 30; -fx-font-weight: bold");
        gpane.setHgap(20);
        gpane.setVgap(20);
        gpane.setStyle("-fx-font-size: 20");
        qualityLbl.setStyle("-fx-text-fill: green; -fx-font-weight: bold");
        qualityValue.setStyle("-fx-font-weight: bold");
        this.getChildren().addAll(header, statusLbl, gpane);
    }

    // Update the pane using the latest results
    public void refresh() {
        AnalysisResult result = AppState.getInstance().getLatestResult();
        if (result == null) {
            statusLbl.setText("No analysis has been completed yet.");
            qualityValue.setText("-");
            confidenceValue.setText("-");
            graphSimilarityValue.setText("-");
            nodeColorValue.setText("-");
            edgeValue.setText("-");
            bestMatchValue.setText("-");
            return;
        }

        statusLbl.setText("Analysis completed successfully.");
        qualityValue.setText(result.getPredictedLabel());
        confidenceValue.setText(String.format("%.2f%%", result.getConfidence() * 100.0));

        SimilarityResult bestMatch = result.getBestMatch();
        graphSimilarityValue.setText(String.format("%.4f", bestMatch.getSimilarity()));
        nodeColorValue.setText(result.getAverageNodeColourDescription());
        edgeValue.setText(result.getEdgeDensityDescription());
        bestMatchValue.setText(bestMatch.getLabel() + " -> " + bestMatch.getRelativePath());
    }
}
