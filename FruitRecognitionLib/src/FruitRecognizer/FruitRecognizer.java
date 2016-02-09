package FruitRecognizer;

import ImageProcessor.ContourRecognizer;
import ImageProcessor.FruitHistogram;
import ImageProcessor.ImageProcessor;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Недко on 31.1.2016 г..
 */
public class FruitRecognizer {
    private final String CONTOUR_DATA_FILE_NAME = "training";
    private double similarityThreshold = 0.2;
    private HashMap<String, ObjectRecognizer> recognizers;

    private class ObjectRecognizer{
        ContourRecognizer contourRecognizer;
        FruitHistogram fruitHistogram;

        ObjectRecognizer()
        {
            contourRecognizer = new ContourRecognizer();
//            fruitHistogram = new FruitHistogram();
        }
    }

    public enum EObjectName{
        GREEN_APPLE("green apple", "./recognitionData/training_green_apple");

        private final String name;
        private final String trainingDataPath;

        EObjectName(String objectName, String trainingDataPath)
        {
            name = objectName;
            this.trainingDataPath = trainingDataPath;
        }
    }

    public FruitRecognizer()
    {
        recognizers = new HashMap<>();
    }

    public void train(String trainingDirPath, String destinationDirPath , String objectName) throws IOException {
        File trainingFile = new File(trainingDirPath);
        File additionalTrainingFile = null;
        File destFile = new File(destinationDirPath);

        if(trainingFile.exists()) {
            if (!trainingFile.isDirectory()) {
                throw new IOException("The chosen training path is not a directory.");
            }
        }
        else
        {
           throw new IOException("No training data.");
        }

//        if(additionalHistogramTrainingDirPath != null && !additionalHistogramTrainingDirPath.equals(""))
//        {
//            additionalTrainingFile = new File(additionalHistogramTrainingDirPath);
//
//            if(additionalTrainingFile.exists()) {
//                if (!additionalTrainingFile.isDirectory()) {
//                    throw new IOException("The chosen training path is not a directory.");
//                }
//            }
//            else
//            {
//                throw new IOException("No training data.");
//            }
//        }

        if (!destFile.exists())
        {
            destFile.mkdirs();
        }

        File contourData = new File(destFile, CONTOUR_DATA_FILE_NAME + "_" + objectName);
        contourData.createNewFile();

        ContourRecognizer contourRecognizer;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;

        List<Mat> images = ImageProcessor.openImages(trainingFile);


        for(Mat image : images) {
            MatOfPoint resultContour = contourRecognizer.train(image);

            if(resultContour != null) {
                //TODO: put file histogram training here
            }

        }
        contourRecognizer.exportTrainingData(contourData);
    }

    //returns false if loading the data was unsuccessful
    public boolean loadTrainingData(String trainingDataPath, String objectName)
    {
        if(trainingDataPath == null || trainingDataPath.isEmpty())
            return false;

        File trainingData = new File(trainingDataPath);

        if(!trainingData.exists() || trainingData.isDirectory())
            return false;

        ContourRecognizer contourRecognizer;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;
        try {
            contourRecognizer.loadTrainingData(trainingData);
        } catch (IOException e) {
            return false;
        }

        return  true;
    }

    private boolean isChild(Rect parent, Rect rect)
    {
        return parent.x < rect.x && parent.y < rect.y &&
                parent.x+parent.width > rect.x+rect.width && parent.y+parent.height > rect.y+rect.height;
    }

    private void removeChildren(List<MatOfPoint> objectContours)
    {
        if(objectContours == null)
            return;

        objectContours.sort(new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                if( Imgproc.boundingRect(o1).area() > Imgproc.boundingRect(o2).area())
                    return 1;

                if( Imgproc.boundingRect(o1).area() < Imgproc.boundingRect(o2).area())
                    return -1;

                return 0;
            }
        });


        for(int k=objectContours.size()-1; k>0; k--)
        {
            Rect rect = Imgproc.boundingRect(objectContours.get(k));
            for(int i=0; i<k; i++)
            {
                Rect smallerRect = Imgproc.boundingRect(objectContours.get(i));
                if(isChild(rect, smallerRect)) {
                    objectContours.remove(i);
                    i--;
                    k--;
                }

            }
        }

    }

    public List<Rectangle> recognize(String imgPath, final EObjectName objectName) throws IOException {
        List<Rectangle> result = null;
        switch(objectName)
        {
            case GREEN_APPLE:
                loadTrainingData(objectName.trainingDataPath, objectName.name());
                result = recognize(imgPath, objectName.name());
                break;

        }

        return  result;
    }

    private List<MatOfPoint> recognizeInternal(String imgPath, String objectName)
    {
        List<MatOfPoint> objectContours = null;

        ContourRecognizer contourRecognizer;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;


        Mat mat = ImageProcessor.openSingleImage(new File(imgPath));
        Mat resized = new Mat();
//        Imgproc.resize(mat, resized, new Size(300, 300));
        objectContours = contourRecognizer.findContours(mat, similarityThreshold);
        //TODO: set up filtering by histogram here

//        removeChildren(objectContours);
        return  objectContours;
    }

    public List<Rectangle> recognize(String imgPath, String objectName) throws IOException {
        List<Rectangle> result = new ArrayList<>();
        List<MatOfPoint> rects = recognizeInternal(imgPath, objectName);


        for(MatOfPoint contour : rects)
        {
            Rect r = Imgproc.boundingRect(contour);
            result.add(new Rectangle(r.x, r.y, r.width, r.height));
        }
//        removeChildren(objectContours);
        return  result;
    }

    public Image recognizeAndDraw(String imgPath, EObjectName objectName) throws IOException {
        Image result = null;
        switch(objectName)
        {
            case GREEN_APPLE:
                loadTrainingData(objectName.trainingDataPath, objectName.name());
                result = recognizeAndDraw(imgPath, objectName.name());
                break;

        }

        return  result;
    }

    public Image recognizeAndDraw(String imgPath, String objectName) throws IOException {

        List<MatOfPoint> contours = recognizeInternal(imgPath, objectName);
        removeChildren(contours);
        Mat resultImg = ImageProcessor.openSingleImage(new File(imgPath));
//        double ratio = resultImg.height()/resultImg.width();
//        Imgproc.resize( resultImg, resultImg, new Size(300, (int)(ratio*300.0)));
        ContourRecognizer.drawContours(resultImg, contours);
       Image result = ImageProcessor.matToImage(resultImg);

        return  result;
    }

    public void setSimilarityThreshold(double threshold)
    {
        similarityThreshold = threshold;
    }
}
