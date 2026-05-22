import acsse.csc3a.graph.Graph;
import acsse.csc3a.results.Results;
import acsse.csc3a.upload.Upload;
import acsse.csc3a.app.AppState;
import acsse.csc3a.about.About;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 * This class is responsible for running the application
 */
public class Main extends Application {

    BorderPane bp1 = new BorderPane();

    Upload upload = new Upload();
    Graph graph = new Graph();
    Results result = new Results();
    About about = new About();

    Button uploadBtn = new Button("Upload");
    Button graphBtn = new Button("Graph");
    Button resultBtn = new Button("Results");
    Button aboutBtn = new Button("About");
    Label header = new Label("Coal Detector");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        try {
            AppState.getInstance().initialise();
        } catch (Exception ex) {
            showError(ex.getMessage());
        }

        VBox vpane2 = new VBox(10);
        header.setStyle("-fx-font-weight: bold; -fx-font-size:28");
        vpane2.getChildren().addAll(uploadBtn, graphBtn, resultBtn, aboutBtn);
        vpane2.setStyle("-fx-background-color: white");

        String defaultStyle = "-fx-background-color: rgb(210,210,210);" +
                "-fx-text-fill: black;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-alignment: CENTER_LEFT;" +
                "-fx-padding: 15 45 15 45;" +
                "-fx-cursor: hand;";

        for (Button button : new Button[]{uploadBtn, graphBtn, resultBtn, aboutBtn}) {
            button.setStyle(defaultStyle);
            button.setMaxWidth(Double.MAX_VALUE);
            VBox.setVgrow(button, null);
        }

        bp1.setCenter(upload);

        uploadBtn.setOnAction(_ -> bp1.setCenter(upload));

        aboutBtn.setOnAction(_ -> bp1.setCenter(about));

        resultBtn.setOnAction(_ -> {
            result.refresh();
            bp1.setCenter(result);
        });

        graphBtn.setOnAction(_ -> {
            graph.refresh();
            bp1.setCenter(graph);
        });

        bp1.setTop(header);
        bp1.setLeft(vpane2);
        Scene scene = new Scene(bp1, 1000, 600);
        primaryStage.setTitle("Coal Quality Detector");
        primaryStage.setScene(scene);
        primaryStage.show();

        String startUpMessage = AppState.getInstance().getStartUpMessage();
        if (startUpMessage != null && startUpMessage.toLowerCase().contains("fail")) {
            showError(startUpMessage);
        }
    }

    private void showError(String message) {
        String text = message;
        if (text == null || text.trim().isEmpty()) {
            text = "An unexpected error occurred.";
        }

        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Coal Quality Detector");
        alert.setContentText(text);
        alert.showAndWait();
    }
}
