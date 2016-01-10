package bg.uni_sofia.fmi.ai.FruitRecognizer;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import bg.uni_sofia.fmi.ai.ImageProcessor.ImageProcessor;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.Mat;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    private ImageView imageView;

    @FXML
    private Button cameraButton;

    Thread cameraThread;
    String resourceImagePath;

    private void startImageMode()
    {
        Mat mat = ImageProcessor.openResource(resourceImagePath);

        ContourRecognizer.findContour(mat);
        Image image = ImageProcessor.matToImage(mat);
        imageView.setImage(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Task<Void> task = new Task<Void>() {
            @Override
            public Void call() throws Exception {
                ImageProcessor imageProcessor = new ImageProcessor();
                Mat webcamMatImage = new Mat();
                Image tempImage;

                VideoCapture capture = new VideoCapture(0);
                capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
                capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
                if( capture.isOpened()) {
                    while (!Thread.currentThread().isInterrupted()) {
                        capture.read(webcamMatImage);
                        if (!webcamMatImage.empty()) {
                            tempImage = ImageProcessor.matToImage(ContourRecognizer.findContour(webcamMatImage));//imageProcessor.toBufferedImage(webcamMatImage);
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
                return null ;
            }
        };
        cameraThread = new Thread(task);
        cameraButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
            if(!cameraThread.isAlive()) {

                cameraThread.start();

            }
            else
            {
                cameraThread.interrupt();
                cameraThread=new Thread(task);
                startImageMode();
            }
        }});
        resourceImagePath = "apple.jpeg";
        startImageMode();
    }
}
