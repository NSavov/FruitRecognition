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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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

    public static Mat openSingleImage(File file)
    {
        if(file.isFile()) {
           return openImages(file).get(0);
        }
        else
            return null;
    }

    public static List<Mat> openImages(File file)
    {
        File[] children = null;
        List<Mat> imgs = new ArrayList<>();
        if(file.isDirectory())
        {
            children = file.listFiles();
        }
        else
        {
            imgs.add(Imgcodecs.imread(file.getAbsolutePath()));
        }

        if(children != null)
        {
            for(File child: children)
            {
                if(child.getName().toLowerCase().endsWith(".jpg") ||
                child.getName().toLowerCase().endsWith(".png") ||
                child.getName().toLowerCase().endsWith(".jpeg"))
                    imgs.add(Imgcodecs.imread(child.getAbsolutePath()));
            }
        }

        return imgs;
    }

    VideoCapture capture;
    public void startCamera(int width, int height)
    {
        capture = new VideoCapture(0);
        capture.set(Videoio.CAP_PROP_FRAME_WIDTH,320);
        capture.set(Videoio.CAP_PROP_FRAME_HEIGHT,240);
    }

    public void getCameraFrame()
    {
//        ImageProcessor imageProcessor = new ImageProcessor();
//        Mat webcamMatImage = new Mat();
//        Image tempImage;
//
//
//        if( capture.isOpened()) {
//            while (true) {
//                capture.read(webcamMatImage);
//                if (!webcamMatImage.empty()) {
//                    tempImage = ImageProcessor.matToImage(ContourRecognizer.findContours(webcamMatImage));//imageProcessor.toBufferedImage(webcamMatImage);
////                    imageLabel.setIcon(imageIcon);
////                    frame.pack(); //this will resize the window to fit the image
//                } else {
//                    System.out.println(" -- Frame not captured -- Break!");
//                    break;
//                }
//            }
//        }
    }

    public static Image matToImage(Mat mat)
    {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }

    public static void findWithCascadeClassifier(Image image)
    {

    }
}
