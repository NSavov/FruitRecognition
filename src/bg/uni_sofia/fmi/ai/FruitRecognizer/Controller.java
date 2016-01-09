package bg.uni_sofia.fmi.ai.FruitRecognizer;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import bg.uni_sofia.fmi.ai.ImageProcessor.ImageProcessor;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ImageView imageView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Image image = ImageProcessor.matToImage(ImageProcessor.openResource("apple.jpeg"));
        imageView.setImage(image);
    }
}
