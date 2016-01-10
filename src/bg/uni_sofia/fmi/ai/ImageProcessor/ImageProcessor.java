package bg.uni_sofia.fmi.ai.ImageProcessor;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import javafx.scene.image.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.ByteArrayInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Недко on 9.1.2016 г..
 */
public class ImageProcessor {
    public static Mat openResource(String filePath)
    {
        Path p1 = Paths.get(".", "resources\\" + filePath).toAbsolutePath();
        Mat mat = Imgcodecs.imread(p1.toString());

        return mat;
    }

    public static Mat openImageFile(String filePath)
    {
        Mat mat = Imgcodecs.imread(filePath);

        return mat;
    }

    VideoCapture capture;
    public void startCamera()
    {
        capture = new VideoCapture(0);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
    }

    public void getCameraFrame()
    {
        ImageProcessor imageProcessor = new ImageProcessor();
        Mat webcamMatImage = new Mat();
        Image tempImage;


        if( capture.isOpened()) {
            while (true) {
                capture.read(webcamMatImage);
                if (!webcamMatImage.empty()) {
                    tempImage = ImageProcessor.matToImage(ContourRecognizer.findContour(webcamMatImage));//imageProcessor.toBufferedImage(webcamMatImage);
//                    imageLabel.setIcon(imageIcon);
//                    frame.pack(); //this will resize the window to fit the image
                } else {
                    System.out.println(" -- Frame not captured -- Break!");
                    break;
                }
            }
        }
    }

    public static Image matToImage(Mat mat)
    {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }
}
