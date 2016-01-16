package bg.uni_sofia.fmi.ai.Contour;

import bg.uni_sofia.fmi.ai.ImageProcessor.ImageProcessor;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Недко on 9.1.2016 г..
 */
public class ContourRecognizer {

    List<MatOfPoint> templates = new ArrayList<>();

    public List<MatOfPoint> getTemplates()
    {
        return templates;
    }

    public void train(File directory)
    {
       train(ImageProcessor.openImages(directory));
    }

    private void train(List<Mat> images)
    {
        int i=0;
        List<MatOfPoint> contours;
        for(Mat img : images)
        {
            contours = getAllContours(img);
            int ind = findLargestContour(contours);
            if(ind < 0)
                continue;

            ArrayList<MatOfPoint> test = new ArrayList<>();
            test.add(contours.get(ind));
            Mat mat = img.clone();
            drawContours(mat, test);
            Imgcodecs.imwrite("./output/training/"+String.valueOf(i) + ".jpg", mat);
            i++;
            templates.add(contours.get(ind));

        }

//        TaFileStorage fileStorage = new TaFileStorage();
//        fileStorage.create("./output/contourData.bin");
//
//        for(int i=0; i<templates.size(); i++) {
//            fileStorage.writeMat("template" + String.valueOf(i), templates.get(i));
//        }

    }

    private void drawContours(Mat img, List<MatOfPoint> contours)
    {
        for(MatOfPoint contour: contours) {
            Rect rect = Imgproc.boundingRect(contour);
            Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
        }
    }

    Mat img;

    public List<MatOfPoint> findContours(Mat img, double threshold)
    {
        List<MatOfPoint> result = new ArrayList<>();
//        try {
//            Thread.currentThread().sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        if(img == null)
            return null;

        List<MatOfPoint> contours = getAllContours(img.clone());
        filter(contours, 100);

//        filter(contours, 0);


        List<Double> values = evaluateContours(contours, templates);

        int size = contours.size();
        for(int i=0 ; i<size; i++)
        {
            double value = values.get(i);
            if(values.get(i)<threshold)
                result.add(contours.get(i));

        }

        Mat mat = img.clone();
//        ArrayList<MatOfPoint> test = new ArrayList<>();
//        test.add(contours.get(findLargestContour(contours)));
        drawContours(img, contours);
//        Imgproc.drawContours(img, contours, 0, new Scalar(0, 0, 255));
        Imgcodecs.imwrite("./output/test2.png",img);

        drawContours(mat, result);
        Imgcodecs.imwrite("./output/test3.png",mat);

        this.img = mat;
        return result;
    }

    public Mat getImage()
    {
        return img;
    }

    public List<MatOfPoint> findContours(File file, double threshold)
    {
        Mat img = ImageProcessor.openSingleImage(file);

        return findContours(img, threshold);
    }

    public int findLargestContourContArea(List<MatOfPoint> contours)
    {
        double maxArea = 0;
        int index = -1;
        for(int i=0; i< contours.size();i++){

            double area = Imgproc.contourArea(contours.get(i));
            if ( area > maxArea ){
                index = i;
                maxArea = area;
            }
        }

        return index;
    }

    public int findLargestContour(List<MatOfPoint> contours)
    {
        double maxArea = 0;
        int index = -1;
        for(int i=0; i< contours.size();i++){

            double area = (Imgproc.boundingRect(contours.get(i))).area();
            if ( area > maxArea ){
                index = i;
                maxArea = area;
            }
        }

        return index;
    }

    public void filter(List<MatOfPoint> contours, int area)
    {
        for(int i=0; i< contours.size();i++){
            double contArea = (Imgproc.boundingRect(contours.get(i))).area();
//            if (Imgproc.contourArea(contours.get(i)) < area ){
            if (contArea < area ){
                contours.remove(i);
                i--;
            }
        }
    }

    int i=0;
    public List<MatOfPoint> getAllContours(Mat mat)
    {

        Mat image = mat.clone();
        Mat imageHSV = new Mat();
        Mat imageBlurr = new Mat();
        Mat imageA = new Mat();
        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2GRAY);
        Imgproc.medianBlur(imageHSV, imageHSV, 9);
        Mat gray0 = new Mat(imageHSV.size(), CvType.CV_8U);
//        Core.mixChannels(imageHSV, gray0, );

//        Imgproc.GaussianBlur(imageHSV, imageHSV, new Size(5,5), 0);
//        Imgproc.adaptiveThreshold(imageBlurr, imageA, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,7, 0);
//        Imgproc.threshold(imageHSV,imageA,15,255, Imgproc.THRESH_BINARY);
//        Imgproc.Canny(imageHSV, imageA, 70, 150, 3, true );

        Imgproc.Canny(imageHSV, imageA, 10, 65, 3, true );

        Imgproc.dilate(imageA, imageA, new Mat());//, new Point(-1, -1), 2);//Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
        Imgproc.erode(imageA, imageA, new Mat());//,new Point(-1, -1), 2);// Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));

//        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
//        Imgproc.morphologyEx(imageHSV, imageA, Imgproc.MORPH_OPEN, element);
//
//        Imgproc.morphologyEx(imageA, imageA, Imgproc.MORPH_ERODE, element);


//        Imgproc.adaptiveThreshold(imageA, imageA, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,7, 0);
//        Imgproc.threshold(imageA,imageA,50,255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite("./output/test1" +".jpeg",imageA);



        i++;

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Imgproc.findContours(imageA, contours, new Mat(), Imgproc.RETR_LIST ,Imgproc.CHAIN_APPROX_NONE);

        Imgcodecs.imwrite("./output/test2.png",image);

        return contours;
    }

    public List<Double> evaluateContours(List<MatOfPoint> contours, List<MatOfPoint> templates)
    {
        ArrayList<Double> values = new ArrayList<>();

        Point centerOfRotation = new Point();
        for (MatOfPoint contour : contours){
            double minValue = Double.MAX_VALUE;
            double tempValue;
            for(MatOfPoint template : templates) {
                centerOfRotation = new Point(contour.rows()/2, contour.cols()/2);
//                MatOfPoint rotatedContour = new MatOfPoint(contour);

//                for (int degrees = 0; degrees < 360; degrees += 20) {

//                    Mat mapMatrix = Imgproc.getRotationMatrix2D(centerOfRotation, degrees, 1);
//                    Imgproc.warpAffine(rotatedContour, rotatedContour, mapMatrix, rotatedContour.size(), Imgproc.INTER_LINEAR);
                    // you could also try Cosine Similarity, or even matchedTemplate here.
                    tempValue = Imgproc.matchShapes(contour, template, Imgproc.CV_CONTOURS_MATCH_I2, 0);

                    if (tempValue < minValue) {
                        minValue = tempValue;
//                    coinRotation = degrees
                    }
//                }


            }
            values.add(minValue);
        }

        return values;
    }

//    public void run(String inFile, String templateFile, String outFile, int match_method) {
//        System.out.println("\nRunning Template Matching");
//
//        Mat img = Highgui.imread(inFile);
//        Mat templ = Highgui.imread(templateFile);
//
//        // / Create the result matrix
//        int result_cols = img.cols() - templ.cols() + 1;
//        int result_rows = img.rows() - templ.rows() + 1;
//        Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
//
//        // / Do the Matching and Normalize
//        Imgproc.matchTemplate(img, templ, result, match_method);
//        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
//
//        // / Localizing the best match with minMaxLoc
//        MinMaxLocResult mmr = Core.minMaxLoc(result);
//
//        Point matchLoc;
//        if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
//            matchLoc = mmr.minLoc;
//        } else {
//            matchLoc = mmr.maxLoc;
//        }
//
//        // / Show me what you got
//        Core.rectangle(img, matchLoc, new Point(matchLoc.x + templ.cols(),
//                matchLoc.y + templ.rows()), new Scalar(0, 255, 0));
//
//        // Save the visualized detection.
//        System.out.println("Writing "+ outFile);
//        Highgui.imwrite(outFile, img);
//
//    }
}
