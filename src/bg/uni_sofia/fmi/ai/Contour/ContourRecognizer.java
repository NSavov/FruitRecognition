package bg.uni_sofia.fmi.ai.Contour;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import sun.awt.image.ImageDecoder;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Недко on 9.1.2016 г..
 */
public class ContourRecognizer {


    public static Mat findContour(Mat mat)
    {
        Mat image = mat;//Imgcodecs.imread("./resources/Golden_01_1.JPG", Imgproc.COLOR_BGR2GRAY);
        Mat imageHSV = new Mat(image.size(), CvType.CV_8U);
        Mat imageBlurr = new Mat(image.size(), CvType.CV_8U);
        Mat imageA = new Mat(image.size(), CvType.CV_8UC3);
        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);

        Imgcodecs.imwrite("./output/test1.jpeg",imageBlurr);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_SIMPLE);

        for(int i=0; i< contours.size();i++){
            System.out.println(Imgproc.contourArea(contours.get(i)));
            if (Imgproc.contourArea(contours.get(i)) > 50 ){
                Rect rect = Imgproc.boundingRect(contours.get(i));
                System.out.println(rect.height);
                if (rect.height > 28){
                    //System.out.println(rect.x +","+rect.y+","+rect.height+","+rect.width);
                    Imgproc.rectangle(image, new Point(rect.x,rect.y), new Point(rect.x+rect.width,rect.y+rect.height),new Scalar(0,0,255));
                }
            }
        }
        Imgcodecs.imwrite("./output/test2.png",image);

        return image;
    }
}
