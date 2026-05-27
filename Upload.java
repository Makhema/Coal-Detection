package coal.detection.upload;

import java.io.File;

import coal.detection.classifier.AnalysisResult;
import coal.detection.app.AppState;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class Upload extends VBox {
    private final Button uploadButton = new Button("Upload Image");
    private final Button analyseButton = new Button("Analyse Image");
    private final Label statusLabel = new Label("Upload an image to begin.");
    private final Label helperLabel = new Label("Supported image files: jpg, jpeg and png");
    public ImageView imagewView = new ImageView();
    private File selectedFile;

    public Upload() {
        setSpacing(15);
        setPadding(new Insets(20));

        uploadButton.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-background-color: blue; -fx-text-fill: white;");
        uploadButton.setPadding(new Insets(10, 20, 10, 20));

        analyseButton.setStyle("-fx-font-size: 15; -fx-font-weight: bold; -fx-background-color: green; -fx-text-fill: white;");
        analyseButton.setPadding(new Insets(10, 20, 10, 20));

        imagewView.setFitWidth(420);
        imagewView.setFitHeight(320);
        imagewView.setPreserveRatio(true);
        imagewView.setStyle("-fx-border-color: grey; -fx-border-width: 1;");

        uploadButton.setOnAction(_ -> chooseImage());
        analyseButton.setOnAction(_ -> analyseSelectedImage());

        HBox buttonRow = new HBox(10, uploadButton, analyseButton);
        this.getChildren().addAll(new Label("Upload"), buttonRow, helperLabel, statusLabel, imagewView);
    }

    private void chooseImage() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Choose a coal image");
        fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedFile = file;
            Image image = new Image(file.toURI().toString());
            imagewView.setImage(image);
            statusLabel.setText("Uploaded image: " + file.getName());
        }
    }

    private void analyseSelectedImage() {
        if (selectedFile == null) {
            showError("Please upload image first.");
            return;
        }

        try {
            AnalysisResult result = AppState.getInstance().analyseExternalImage(selectedFile);
            statusLabel.setText("Analysis complete. Predicted quality: " + result.getPredictedLabel());
        } catch (Exception ex) {
            String message = ex.getMessage();

            if (message == null || message.trim().isEmpty()) {
                message = "Failed to analyse image.";
            }

            statusLabel.setText(message);
            showError(message);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Coal Quality Detector");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public ImageView getImageView() {
        return imagewView;
    }
}
