package bg.uni_sofia.fmi.ai.ImageProcessor;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yordan on 1/13/2016.
 */
public class FruitHistogram {
    public Mat result;
    public Mat mask;
    public Mat image, histogram;
    public String fruitName;
    public File trainingPath;
    public ContourRecognizer contourRecognizer;
    public List<Mat> histograms;

    public String path;

    private MatOfPoint2f toMatOfPoint2f(MatOfPoint m) {
        return new MatOfPoint2f(m.toArray());
    }

    public FruitHistogram(ContourRecognizer contourRecognizer, String triningPath, String fruit) {
        this.fruitName = fruit;
        this.contourRecognizer = contourRecognizer;
        this.trainingPath = new File(triningPath);
        this.histograms = new ArrayList<Mat>();


    }

    public void train() {
        if(!this.trainingPath.exists()) {
            return ;
        }
        for(File image : this.trainingPath.listFiles()) {
            this.histograms.add(makeHistogram(image));
        }
    }

    public float compare(Mat image) {
        Mat tempHistogram = makeHistogram(image);
        float maxComparement = 0;
        for(Mat hist : this.histograms) {
            maxComparement = (float)Math.max(maxComparement, Imgproc.compareHist(hist, tempHistogram, 0));
        }
        return maxComparement;

    }
    private Mat makeHistogram(Mat image) {
        List<MatOfPoint> result = contourRecognizer.findContours(image, 0.2);
        Mat mask = Mat.zeros(image.height(),image.width(), CvType.CV_8U);

        for (int i = 0 ; i < result.size(); i ++) {
            Imgproc.drawContours(mask, result, i, new Scalar(255,255,255), -1);
        }
        List<Mat> images = new ArrayList<Mat>();
        images.add(image);
        histogram = new Mat();
        Imgproc.calcHist(images, new MatOfInt(1), mask, histogram, new MatOfInt(25), new MatOfFloat(0,256));
//        Imgcodecs.imwrite("./output/training/masks/" + imagePath.getName(), mask);
        return histogram;

    }
    private Mat makeHistogram(File imagePath) {
        File outputFile = new File("./output/training/masks");
        if (!outputFile.isDirectory()) {
            outputFile.mkdir();
        }
        return makeHistogram(ImageProcessor.openSingleImage(imagePath));
    }

}
