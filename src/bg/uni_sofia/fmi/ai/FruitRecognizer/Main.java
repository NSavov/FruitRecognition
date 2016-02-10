package bg.uni_sofia.fmi.ai.FruitRecognizer;

import ImageProcessor.ContourRecognizer;
import ImageProcessor.ImageProcessor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.KNearest;
import org.opencv.ml.Ml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Main extends Application {

    static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader =new FXMLLoader(getClass().getResource("layout_main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Fruit Recognizer");
        Scene scene = new Scene(root, 300, 275);
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = fxmlLoader.getController();
        controller.setStage(primaryStage);

        ContourRecognizer contourRecognizer = new ContourRecognizer();
        Mat image = ImageProcessor.openSingleImage(new File("./resources/images.jpg"));;//Imgcodecs.imread("./resources/Golden_01_1.JPG", Imgproc.COLOR_BGR2GRAY);
        Mat imageHSV = new Mat(image.size(), CvType.CV_8U);
        Mat imageBlurr = new Mat(image.size(), CvType.CV_8U);
        Mat imageA = new Mat(image.size(), CvType.CV_8UC3);
//        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.GaussianBlur(imageHSV, imageBlurr, new Size(5,5), 0);
//        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY,7, 5);
//       System.out.println(imageA.dump());
        Mat m = new Mat(1, 1, CvType.CV_32F, new Scalar(0));
        Mat m2 = new Mat(1, 1, CvType.CV_32F, new Scalar(5));
        Mat m3 = new Mat(1, 1, CvType.CV_32F, new Scalar(10));
        Mat response = new Mat(), dist = new Mat();
        Mat mAll = new Mat();
        mAll.push_back(m);
        mAll.push_back(m2);
        mAll.push_back(m3);
        Mat histogram = new Mat();
        List<Mat> images = new ArrayList<Mat>();
        images.add(image);
        Imgproc.calcHist(images, new MatOfInt(0), new Mat(), histogram, new MatOfInt(5), new MatOfFloat(0,256));
//        Mat result = FruitHistogram.matFromJson(FruitHistogram.matToJson(histogram));
//        List<Mat> test = new ArrayList<Mat>();
//        test.add(histogram);
//        test.add(histogram);
//        test.add(histogram);
//
//        System.out.println(FruitHistogram.matFromJson(FruitHistogram.matToJson(test)));

        Mat mResult = new Mat(1, 3, CvType.CV_32F, new Scalar(0));
        mResult.put(0,0, new double[] {1});
        mResult.put(0,1, new double[] {2});
        mResult.put(0,2, new double[] {3});
//
//  byte[] jk = {1,2,3};
//        m.put(0,0, jk);
////        m.put(0,0, p);
        System.out.println(mAll.dump());
//        m.convertTo(m, CvType.CV_32F);
        KNearest k = KNearest.create();
        k.train(mAll, Ml.ROW_SAMPLE, mResult);
        k.findNearest(new Mat(1,1,CvType.CV_32F,new Scalar(3.6)),1,mResult,response,dist);
        System.out.println(response.dump());
        System.out.println(dist.dump());
//        FruitHistogram f = new FruitHistogram();
//
//        System.out.println("OpenCV Mat: " + m);
//        Mat mr1 = m.row(1);
//        mr1.setTo(new Scalar(1));
//        Mat mc5 = m.col(5);
//        mc5.setTo(new Scalar(5));
//        System.out.println("OpenCV Mat data:\n" + m.dump());

    }

    public static void main(String[] args) {
        launch(args);
    }
}
