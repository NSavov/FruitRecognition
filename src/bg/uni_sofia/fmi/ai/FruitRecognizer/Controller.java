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
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
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


    Stage stage;
    Thread cameraThread;
    String resourceImagePath;
    ContourRecognizer contourRecognizer;

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
    }

    private void startImageMode()
    {

        contourRecognizer.findContours(new File(resourceImagePath), 0.2);
        Mat mat = contourRecognizer.getImage();
        Image image = ImageProcessor.matToImage(mat);
        imageView.setImage(image);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        contourRecognizer = new ContourRecognizer();

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
        resourceImagePath = "./resources/apple.jpg";
        contourRecognizer.train(new File("./resources/dataset"));
        startImageMode();
    }
}
