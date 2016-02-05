package bg.uni_sofia.fmi.ai.FruitRecognizer;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import bg.uni_sofia.fmi.ai.ImageProcessor.ImageProcessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ImageView imageView;

    @FXML
    private Button cameraButton;

    @FXML
    private Button recognizeFromImageButton;

    @FXML
    private Button trainButton;

    @FXML
    private ComboBox objectListView;

    Stage stage;
    Thread cameraThread;
    String resourceImagePath;
    ContourRecognizer contourRecognizer;
    FruitRecognizer fruitRecognizer;

    class CaptureRunnable implements Runnable{
        @Override
        public void run() {
            ImageProcessor imageProcessor = new ImageProcessor();
            Mat webcamMatImage = new Mat();
            Image tempImage;

            VideoCapture capture = new VideoCapture(0);
            capture.set(Videoio.CAP_PROP_FRAME_WIDTH,1024);
            capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,768);
            if( capture.isOpened()) {
                while (!Thread.currentThread().isInterrupted()) {
                    capture.read(webcamMatImage);
                    if (!webcamMatImage.empty()) {
                        contourRecognizer.findContours(webcamMatImage, 0.2);
                        tempImage = ImageProcessor.matToImage(contourRecognizer.getImage());//imageProcessor.toBufferedImage(webcamMatImage);

                        if(Thread.currentThread().isInterrupted())
                            break;
                        imageView.setImage(tempImage);
//                    imageLabel.setIcon(imageIcon);
//                    frame.pack(); //this will resize the window to fit the image
                    } else {
                        System.out.println(" -- Frame not captured -- Break!");
                        break;
                    }
                }
            }
            capture.release();
        }
    }

    public void setStage(Stage stage)
    {
        this.stage = stage;
        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                if(cameraThread != null && cameraThread.isAlive())
                    cameraThread.interrupt();
            }
        });

        imageView.fitWidthProperty().bind(stage.widthProperty());
//        imageView.fitHeightProperty().bind(stage.heightProperty());
    }

    private void startImageMode()
    {

        List<MatOfPoint> list = contourRecognizer.findContours(new File(resourceImagePath), 0.2);
        Mat mat = contourRecognizer.getImage();
        Image image = ImageProcessor.matToImage(mat);
        imageView.setImage(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        contourRecognizer = new ContourRecognizer();
        fruitRecognizer = new FruitRecognizer();

        cameraThread = new Thread(new CaptureRunnable());
        cameraButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            if(!cameraThread.isAlive()) {

                cameraThread=new Thread(new CaptureRunnable());
                cameraThread.start();

            }
            else
            {
                cameraThread.interrupt();
                startImageMode();
            }
        }});

        recognizeFromImageButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Open Image");
                fileChooser.getExtensionFilters().addAll(
                        new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif", "*.jpeg"));
                File file = fileChooser.showOpenDialog(stage);
                if(file != null) {
                    imageView.setImage(new Image(file.toURI().toString()));
                }

                try {
                    fruitRecognizer.recognize(file.getAbsolutePath(), FruitRecognizer.EObjectName.GREEN_APPLE);
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

                    TextInputDialog dialog = new TextInputDialog("");
                    dialog.setTitle("Fruit Name Dialog");
                    dialog.setHeaderText("What is the name of this fruit?");
                    dialog.setContentText("Please enter the name of the fruit:");

                    String  objectName = null;
                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()){
                        objectName=result.get();
                    }

                if(objectName != null)
                {
                    try {
                        fruitRecognizer.train(file.getAbsolutePath(), "./", objectName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                objectListView.getItems().add(objectName);
            }
        });

        resourceImagePath = "./resources/apple.jpg";
//        try {
//            contourRecognizer.train(new File("./resources/dataset"), new File("./output/data.txt"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        try {
            contourRecognizer.loadTrainingData(new File("./output/data.txt"));
//            FruitHistogram fr = new FruitHistogram(contourRecognizer, "D:\\Work\\FruitRecognition\\resources\\dataset", "apple");
//            fr.train();
//            System.out.println(fr.compare(ImageProcessor.openSingleImage(new File(resourceImagePath)))+ "________________");
        } catch (IOException e) {
            e.printStackTrace();
        }

        startImageMode();

    }
}
