package bg.uni_sofia.fmi.ai.ImageProcessor;

import javafx.scene.image.Image;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

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
        Mat mat = Imgcodecs.imread(p1.toString(), CvType.CV_8UC3);

        return mat;
    }

    public static Mat openImageFile(String filePath)
    {
        Mat mat = Imgcodecs.imread(filePath, CvType.CV_8UC3);

        return mat;
    }

    public static Image matToImage(Mat mat)
    {
        MatOfByte byteMat = new MatOfByte();
        Imgcodecs.imencode(".bmp", mat, byteMat);
        return new Image(new ByteArrayInputStream(byteMat.toArray()));
    }
}
