package bg.uni_sofia.fmi.ai.FruitRecognizer;

import FruitRecognizer.FruitRecognizer;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ImageView imageView;

    @FXML
    private ScrollPane scrollPane;

    @FXML
    private Button recognizeFromImageButton;

    @FXML
    private Button trainButton;

    @FXML
    private Button loadTrainingDataButton;

    @FXML
    private ComboBox objectListView;

    Stage stage;
    Image image;
    FruitRecognizer fruitRecognizer;

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
//                if(cameraThread != null && cameraThread.isAlive())
//                    cameraThread.interrupt();
            }
        });

        imageView.fitWidthProperty().bind(scrollPane.widthProperty().subtract(15));
//        imageView.fitHeightProperty().bind(stage.heightProperty());
    }

    private void startImageMode()
    {
        imageView.setImage(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        fruitRecognizer = new FruitRecognizer();
        try {
            fruitRecognizer.train("D:\\git\\FruitRecognition\\FruitRecognition\\resources\\dataset", "D:\\git\\FruitRecognition\\FruitRecognitionLib\\resources\\recognitionData", "GREEN_APPLE");
        } catch (IOException e) {
            e.printStackTrace();
        }
        recognizeFromImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Image");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"));
                File file = fileChooser.showOpenDialog(stage);

                if(file == null)
                    return;

                try {
                    FruitRecognizer.EObjectName.values();
                    String objectName = (String)objectListView.getValue();
                    boolean foundDefault = false;
                    for(FruitRecognizer.EObjectName obj : FruitRecognizer.EObjectName.values())
                    {
                        if(obj.name.toLowerCase().equals(objectName.toLowerCase())) {
                            image = fruitRecognizer.recognizeAndDraw(file.getAbsolutePath(), obj);
                            foundDefault = true;
                            break;
                        }
                    }

                    if(!foundDefault)
                        image = fruitRecognizer.recognizeAndDraw(file.getAbsolutePath(), objectName);
                    imageView.setImage(image);
                } catch (IOException e) {
                    e.printStackTrace();
                }
//                fruitRecognizer.recognize();
            }
        });

        trainButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                    DirectoryChooser directoryChooser = new DirectoryChooser();
                    directoryChooser.setTitle("Open Directory");
                    File file = directoryChooser.showDialog(stage);

                    if(file == null)
                      return;

                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Fruit Name Dialog");
                    dialog.setHeaderText("What is the name of this fruit?");
                    dialog.setContentText("Please enter the name of the fruit:");

                    String  objectName = null;
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()){
                        objectName=result.get();
                    }
                else
                    {
                        return;
                    }

                if(objectName != null)
                {
                    try {
                        fruitRecognizer.train(file.getAbsolutePath(), "./", objectName);
                        objectListView.getItems().add(objectName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        });

        loadTrainingDataButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser contourfileChooser = new FileChooser();
                contourfileChooser.setTitle("Open Contour Data");
                contourfileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Training Data", "*.contour"));
                File file = contourfileChooser.showOpenDialog(stage);

                if(file == null)
                    return;

                FileChooser histogramfileChooser = new FileChooser();
                histogramfileChooser.setTitle("Open Contour Data");
                histogramfileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Training Data", "*.contour"));
                File histogramFile = histogramfileChooser.showOpenDialog(stage);

                if(histogramFile == null)
                    return;

                TextInputDialog dialog = new TextInputDialog("");
                dialog.setTitle("Fruit Name Dialog");
                dialog.setHeaderText("What is the name of this fruit?");
                dialog.setContentText("Please enter the name of the fruit:");

                String  objectName = null;
                Optional<String> result = dialog.showAndWait();
                if (result.isPresent()){
                    objectName=result.get();
                }
                else
                {
                    return;
                }

                try {
                    fruitRecognizer.loadTrainingData(file.getPath(), histogramFile.getPath(), objectName);
                    objectListView.getItems().add(objectName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });

        image = new Image(Controller.class.getResource("/title.png").toString());
        startImageMode();

    }
}
