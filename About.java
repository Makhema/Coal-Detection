package acsse.csc3a.about;

import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;


public class About extends VBox {

    Label about = new Label("South Africa is the biggest coal producer in the world, with over 100 coal \r\n"
			+ "mines across the country. Coal is responsible for about 74-80% of the \r\n"
			+ "country's electricity generation. The quality of coal is not always the \r\n"
			+ "same as it may contain impurities and it may also be affected by how it \r\n"
			+ "looks and feel. Good quality of coal is very important to the South African \r\n"
			+ "economy as bad quality contribute significantly to load shedding. The \r\n"
			+ "quality of coal is normally determined by testing it in the laboratory, \r\n"
			+ "which may be time consuming and expensive. Our project purpose is to \r\n"
			+ "develop a desktop application that uses graph-based structure to \r\n"
			+ "differentiate a sample of coal images. Our application checks things like \r\n"
			+ "the texture of the coal, the colours and how the particles are spread out \r\n"
			+ "to help us figure out how good the quality of coal is. Our application will \r\n"
			+ "represent image features as nodes in a graph data structure while \r\n"
			+ "relationships between pixels or regions are represented as edges. The \r\n"
			+ "application performs graph algorithms to analyse coal samples by \r\n"
			+ "capturing spatial relationships between image regions, which is achieved \r\n"
			+ "by modelling image as a graph. Our project will implement similarity \r\n"
			+ "detection and classification using graph neural networks (GNNs). \r\n"
			+ "Similarity detection will allow the application to compare the new coal \r\n"
			+ "image with all the existing images in the database to determine whether \r\n"
			+ "the new coal sample has characteristics that are similar to those of the \r\n"
			+ "samples already in the database. Classification using graph neural \r\n"
			+ "networks (GNNs) will allow our application to analyse the relationships \r\n"
			+ "between pixels within the image graph and classify the coal sample \r\n"
			+ "based on learned patterns and structural features. ");
    FlowPane fpane = new FlowPane();
    Label header = new Label("About");

    public About() {
        about.setStyle("-fx-font-size: 20");
        about.setWrapText(true);
        header.setStyle("-fx-font-size: 30; -fx-font-weight: bold");
        fpane.getChildren().add(about);
        ScrollPane sp = new ScrollPane(fpane);
        sp.setFitToWidth(true);
        this.getChildren().addAll(header, sp);
    }
}
