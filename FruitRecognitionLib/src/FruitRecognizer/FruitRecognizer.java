package FruitRecognizer;

import ImageProcessor.ContourRecognizer;
import ImageProcessor.FruitHistogram;
import ImageProcessor.ImageProcessor;
import javafx.scene.image.Image;
import javafx.scene.shape.Rectangle;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.*;

/**
 * Created by Недко on 31.1.2016 г..
 */
public class FruitRecognizer {

    private static final String CONTOUR_DATA_FILE_NAME = "training";
    private static final String HISTOGRAM_DATA_FILE_NAME = "histogram";
    private double similarityThreshold = 0.2;
    private HashMap<String, ObjectRecognizer> recognizers;

    /**
     * Puts library to temp dir and loads to memory
     */
    private static void loadLib() {
        String architecture  = "x64";
        if(System.getProperty("os.arch").equals("x86"))
        {
            architecture = "x86";
        }
            // have to use a stream
            InputStream in = FruitRecognizer.class.getResourceAsStream("/" + architecture + "/" + Core.NATIVE_LIBRARY_NAME + ".dll");
            // always write to different location
            File fileOut = new File(System.getProperty("java.io.tmpdir") + "/" + Core.NATIVE_LIBRARY_NAME + ".dll");
        OutputStream out = null;
        try {
            out = FileUtils.openOutputStream(fileOut);
            IOUtils.copy(in, out);

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
            System.load(fileOut.toString());
    }

    private class ObjectRecognizer{
        ContourRecognizer contourRecognizer;
        FruitHistogram fruitHistogram;

        ObjectRecognizer()
        {
            contourRecognizer = new ContourRecognizer();
            fruitHistogram = new FruitHistogram();
        }
    }

    public enum EObjectName{
        GREEN_APPLE("green apple", "/recognitionData/" + CONTOUR_DATA_FILE_NAME + "_green_apple",
                "/recognitionData/" + HISTOGRAM_DATA_FILE_NAME + "_green_apple");

        private final String name;
        private final InputStream trainingDataPathContours;
        private final InputStream trainingDataPathHistograms;

        EObjectName(String objectName, String trainingDataPathContours, String trainingDataPathHistograms)
        {
            name = objectName;
            this.trainingDataPathContours = FruitRecognizer.class.getResourceAsStream(trainingDataPathContours);
            this.trainingDataPathHistograms = FruitRecognizer.class.getResourceAsStream(trainingDataPathHistograms);
        }
    }

    public FruitRecognizer()
    {
        loadLib();
        recognizers = new HashMap<>();
    }

    public void train(String trainingDirPath, String destinationDirPath , String objectName) throws IOException {
        File trainingFile = new File(trainingDirPath);
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

        if (!destFile.exists())
        {
            destFile.mkdirs();
        }

        File contourData = new File(destFile, CONTOUR_DATA_FILE_NAME + "_" + objectName);
        File histogramData = new File(destFile, HISTOGRAM_DATA_FILE_NAME + "_" + objectName);
        contourData.createNewFile();
        histogramData.createNewFile();

        ContourRecognizer contourRecognizer;
        FruitHistogram fruitHistogram;
        if(!recognizers.containsKey(objectName)) {
            recognizers.put(objectName, new ObjectRecognizer());
        }
        ObjectRecognizer objectRecognizer =  recognizers.get(objectName);
        contourRecognizer = objectRecognizer.contourRecognizer;
        fruitHistogram = objectRecognizer.fruitHistogram;

        List<Mat> images = ImageProcessor.openImages(trainingFile);

        for(Mat image : images) {
            MatOfPoint resultContour = contourRecognizer.train(image);
            if(resultContour != null) {
                fruitHistogram.train(resultContour, image);
            }
        }

        contourRecognizer.exportTrainingData(contourData);
        fruitHistogram.exportTrainingData(histogramData);
    }

    public boolean loadTrainingDataInternal(InputStream trainingDataContours, InputStream trainingDataHistograms, String objectName)
    {
        ContourRecognizer contourRecognizer;
        FruitHistogram fruitHistogram;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        ObjectRecognizer objectRecognizer =  recognizers.get(objectName);
        contourRecognizer = objectRecognizer.contourRecognizer;
        fruitHistogram = objectRecognizer.fruitHistogram;
        try {
            contourRecognizer.loadTrainingData(trainingDataContours);
            fruitHistogram.loadTrainingData(trainingDataHistograms);
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    //returns false if loading the data was unsuccessful
    public boolean loadTrainingData(String trainingDataPathContours, String trainingDataPathHistograms, String objectName) throws FileNotFoundException {
        if(trainingDataPathContours == null || trainingDataPathContours.isEmpty() || trainingDataPathHistograms == null || trainingDataPathHistograms.isEmpty())
            return false;

        File trainingDataContours = new File(trainingDataPathContours);
        File trainingDataHistograms = new File(trainingDataPathHistograms);
        if(!trainingDataContours.exists() || trainingDataContours.isDirectory() || !trainingDataHistograms.exists() || trainingDataHistograms.isDirectory())
            return false;

        return loadTrainingDataInternal(new FileInputStream(trainingDataContours), new FileInputStream(trainingDataHistograms), objectName);

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
                loadTrainingDataInternal(objectName.trainingDataPathContours, objectName.trainingDataPathHistograms, objectName.name());
                result = recognize(imgPath, objectName.name());
                break;

        }

        return  result;
    }

    private List<MatOfPoint> recognizeInternal(String imgPath, String objectName)
    {
        List<MatOfPoint> objectContours = null;

        ContourRecognizer contourRecognizer;
        FruitHistogram fruitHistogram;
        if(!recognizers.containsKey(objectName))
            recognizers.put(objectName, new ObjectRecognizer());

        contourRecognizer = recognizers.get(objectName).contourRecognizer;
        fruitHistogram = recognizers.get(objectName).fruitHistogram;

        Mat mat = ImageProcessor.openSingleImage(new File(imgPath));
        Mat resized = new Mat();
//        Imgproc.resize(mat, resized, new Size(300, 300));
        objectContours = contourRecognizer.findContours(mat, similarityThreshold);
        //TODO: set up filtering by histogram here
//        System.out.println("Tova trqbva da e chisloto " + fruitHistogram.compare(objectContours, mat));
        removeChildren(objectContours);
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
        return  result;
    }

    public Image recognizeAndDraw(String imgPath, EObjectName objectName) throws IOException {
        Image result = null;
        switch(objectName)
        {
            case GREEN_APPLE:
                loadTrainingDataInternal(objectName.trainingDataPathContours, objectName.trainingDataPathHistograms, objectName.name());
                result = recognizeAndDraw(imgPath, objectName.name());
                break;

        }

        return  result;
    }

    public Image recognizeAndDraw(String imgPath, String objectName) throws IOException {

        List<MatOfPoint> contours = recognizeInternal(imgPath, objectName);
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
