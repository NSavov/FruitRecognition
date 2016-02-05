package bg.uni_sofia.fmi.ai.FruitRecognizer;

import bg.uni_sofia.fmi.ai.Contour.ContourRecognizer;
import bg.uni_sofia.fmi.ai.ImageProcessor.FruitHistogram;
import bg.uni_sofia.fmi.ai.ImageProcessor.ImageProcessor;
import org.opencv.core.Mat;

import java.awt.*;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
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
        GREEN_APPLE("green apple", "./recognitionData/green_apple");

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

        File contourData = new File(trainingFile, CONTOUR_DATA_FILE_NAME + "_" + objectName);
        contourData.createNewFile();

        ContourRecognizer contourRecognizer;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;
        contourRecognizer.train(trainingFile, contourData);
        //TODO: put file histogram training here

//        if(additionalTrainingFile != null)
//        {
//            //TODO: put file histogram training here
//        }
    }

    //returns false if loading the data was unsuccessful
    public boolean loadTrainingData(String trainingDataPath, String objectName)
    {
        if(trainingDataPath == null || trainingDataPath.isEmpty())
            return false;

        File trainingData = new File(trainingDataPath);

        if(!trainingData.exists() || !trainingData.isDirectory())
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

    public List<List<Double>> recognize(String imgPath, final EObjectName objectName) throws IOException {
        List<List<Double>> result = null;
        switch(objectName)
        {
            case GREEN_APPLE:
                loadTrainingData(objectName.trainingDataPath, objectName.name());
                result = recognize(imgPath, objectName.name());
                break;

        }

        return  result;
    }

    public List<List<Double>> recognize(String imgPath, String objectName) throws IOException {
        List<List<Double>> result = null;

        ContourRecognizer contourRecognizer;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;

        Mat mat = ImageProcessor.openSingleImage(new File(imgPath));
        contourRecognizer.findContours(mat, similarityThreshold);
        //TODO: set up filtering by histogram here

        return  result;
    }


    public void setSimilarityThreshold(double threshold)
    {
        similarityThreshold = threshold;
    }
}
