package ImageProcessor;

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

    public void trainFromDir(File directory, File dest) throws IOException {
       trainFromDir(ImageProcessor.openImages(directory), dest);
    }

    private void trainFromDir(List<Mat> images, File dest) throws IOException {
        int k=0;
        List<MatOfPoint> contours;
        for(Mat img : images)
        {
            contours = getAllContours(img);
            int ind = findLargestContour(contours);
            if(ind < 0)
                continue;

            ArrayList<MatOfPoint> test = new ArrayList<>();
            final MatOfPoint largestContour = contours.get(ind);
            List<MatOfPoint> solidLargestContour = new ArrayList<MatOfPoint>(){{add(largestContour);}};
            solidityProcessing(solidLargestContour);
            test.add(solidLargestContour.get(0));
            Mat mat = img.clone();
            drawContours(mat, test);
            Imgcodecs.imwrite("./output/training/"+String.valueOf(k) + ".jpg", mat);
            k++;
            templates.add(solidLargestContour.get(0));
        }

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(dest));
        for(MatOfPoint template : templates) {
            List<Point> points = template.toList();

            for (Point point : points) {
                writer.write(String.valueOf(point.x));
                writer.write(' ');
                writer.write(String.valueOf(point.y));
                writer.write(' ');
            }

            writer.write("\n");
        }
        writer.close();
    }


    public MatOfPoint train(File file, File dest) throws IOException {
        if(!file.isFile())
            return null;

        return train(ImageProcessor.openSingleImage(file));

    }

    public MatOfPoint train(Mat img) throws IOException {

        List<MatOfPoint> contours;

            contours = getAllContours(img);
            int ind = findLargestContour(contours);
            if(ind < 0)
                return null;
        List<MatOfPoint> solidLargestContour = new ArrayList<MatOfPoint>(){{add(contours.get(ind));}};
        solidityProcessing(solidLargestContour);
            templates.add(solidLargestContour.get(0));

        return solidLargestContour.get(0);
    }

    public void exportTrainingData(File dest) throws IOException {

        BufferedWriter writer = null;
        writer = new BufferedWriter(new FileWriter(dest));

        for(MatOfPoint template : templates) {
            List<Point> points = template.toList();

            for (Point point : points) {
                writer.write(String.valueOf(point.x));
                writer.write(' ');
                writer.write(String.valueOf(point.y));
                writer.write(' ');
            }

            writer.write("\n");
        }

        writer.close();
    }

    public boolean isTrainingDataLoaded()
    {
        return templates != null && !templates.isEmpty();
    }

    public void loadTrainingData(InputStream src) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(src));
        String line;

        if(templates == null)
            templates = new ArrayList<>();

        List<Point> points = new ArrayList<>();
        while((line = reader.readLine()) != null) {
            line = line.trim();
            String[] numbers = line.split(" ");

            if(numbers.length < 2)
                throw new IOException();

            for (int i = 1; i<numbers.length; i+=2) {
                points.add(new Point(Double.valueOf(numbers[i-1]), Double.valueOf(numbers[i])));
            }
            MatOfPoint matOfPoint = new MatOfPoint();
            matOfPoint.fromList(points);
            templates.add(matOfPoint);
            points.clear();
        }
    }

    public static void drawContours(Mat img, List<MatOfPoint> contours)
    {
        for(MatOfPoint contour: contours) {
            Rect rect = Imgproc.boundingRect(contour);
            Imgproc.rectangle(img, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255));
        }
    }

    private double contourSolidity = 0.6;

    public void setContourSolidity(double solidity)
    {
        contourSolidity = solidity;
    }

    private void solidityProcessing(List<MatOfPoint> contours)
    {
        //solidity check
        for(int i=0; i<contours.size(); i++) {
            MatOfPoint contour = contours.get(i);
            MatOfInt hull = new MatOfInt();
            Imgproc.convexHull(contour, hull);
            MatOfPoint hullContour = hull2Points(hull, contour);
            double contourArea = Imgproc.contourArea(contour);
            double hullArea = Imgproc.contourArea(hullContour);

            if(contourArea/hullArea < contourSolidity) {
                contours.remove(i);
                contours.add(i, hullContour);
            }

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
//        filter(contours, (Math.max(img.rows(), img.cols())/17)*(Math.min(img.rows(), img.cols())/17));

        filterByHeight(contours, img.rows()/20);
        filterByWidth(contours, img.cols()/20);
        filterByHeight(contours, 150);
        filterByWidth(contours, 150);

        solidityProcessing(contours);
        List<Double> values = evaluateContours(contours, templates);

        int size = contours.size();
        for(int i=0 ; i<size; i++)
        {
            double value = values.get(i);
            if(values.get(i)<threshold)
                result.add(contours.get(i));

        }

        Mat mat = img.clone();
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

    private int findLargestContourContArea(List<MatOfPoint> contours)
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

    private int findLargestContour(List<MatOfPoint> contours)
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

    private void filter(List<MatOfPoint> contours, int area)
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

    private void filterByHeight(List<MatOfPoint> contours, int height)
    {
        for(int i=0; i< contours.size();i++){
            double rectHeight = (Imgproc.boundingRect(contours.get(i))).height;
            if (rectHeight < height ){
                contours.remove(i);
                i--;
            }
        }
    }

    private void filterByWidth(List<MatOfPoint> contours, int width)
    {
        for(int i=0; i< contours.size();i++){
            double rectWidth = (Imgproc.boundingRect(contours.get(i))).width;
            if (rectWidth < width ){
                contours.remove(i);
                i--;
            }
        }
    }

//    public Mat decolor(Mat img)
//    {
//        Mat output = new Mat();
//        Core.inRange(img, new Scalar(0, 0, 0), new Scalar(255, 10, 10),output);
//        Imgcodecs.imwrite("./output/test5.png", output);
//        return output;
//    }


    private List<MatOfPoint> getAllContours(Mat mat)
    {

        Mat image = mat.clone();
        Mat imageHSV = new Mat();

//        Imgproc.cvtColor(image, imageHSV, Imgproc.COLOR_BGR2HSV);
//        imageHSV = decolor(image);


//        Imgcodecs.imwrite("./output/testn.png",image);
//        Imgproc.cvtColor(imageHSV, imageHSV, Imgproc.COLOR_BGR2GRAY);
//        Imgproc.medianBlur(imageHSV, imageHSV, 9);

        List<Mat> src = new ArrayList<Mat>();
        src.add(image);
        List<Mat> dest = new ArrayList<Mat>();
        Mat gray0 = new Mat(image.size(), CvType.CV_8U);
        dest.add(gray0);
        MatOfInt convert;

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        for(int i=0; i<3; i++) {
            convert = new MatOfInt(i, 0);
            Core.mixChannels(src, dest, convert);
            Imgcodecs.imwrite("./output/test4-" + String.valueOf(i) + ".jpeg", dest.get(0));

            imageHSV = dest.get(0);
//        Imgproc.GaussianBlur(imageHSV, imageHSV, new Size(5,5), 0);
//        Imgproc.adaptiveThreshold(imageHSV, imageA, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,7, 0);
//        Imgproc.threshold(imageHSV,imageHSV,15,255, Imgproc.THRESH_BINARY);
//            Imgproc.Canny(imageHSV, imageHSV, 70, 150, 3, true);

        Imgproc.Canny(imageHSV, imageHSV, 20, 80, 3, true );
//        Photo.fastNlMeansDenoising(imageHSV, imageHSV);
//        Imgproc.dilate(imageHSV, imageHSV, new Mat(), new Point(-1, -1), 2);//Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2)));
//        Imgproc.erode(imageHSV, imageHSV, new Mat(),new Point(-1, -1), 2);// Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2,2)));

            Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(2, 2));
        Imgproc.morphologyEx(imageHSV, imageHSV, Imgproc.MORPH_CLOSE, element);

            Imgproc.morphologyEx(imageHSV, imageHSV, Imgproc.MORPH_DILATE, element, new Point(-1, -1), 2);


//        Imgproc.adaptiveThreshold(imageHSV, imageHSV, 255,Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY,7, 0);
//        Imgproc.threshold(imageHSV,imageHSV,50,255, Imgproc.THRESH_BINARY);
            Imgcodecs.imwrite("./output/test1-" + String.valueOf(i) + ".jpeg", imageHSV);


            List<MatOfPoint> temp = new ArrayList<MatOfPoint>();
            Imgproc.findContours(imageHSV, temp, new Mat(), Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_NONE);

            contours.addAll(temp);
        }

        return contours;
    }


    private MatOfPoint hull2Points(MatOfInt hull, MatOfPoint contour) {
        List<Integer> indexes = hull.toList();
        List<Point> points = new ArrayList<>();
        MatOfPoint point= new MatOfPoint();
        for(Integer index:indexes) {
            points.add(contour.toList().get(index));
        }
        point.fromList(points);
        return point;
    }

    private List<Double> evaluateContours(List<MatOfPoint> contours, List<MatOfPoint> templates)
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
}
