package ImageProcessor;

import com.google.gson.*;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yordan on 1/13/2016.
 */
public class FruitHistogram {
    public static final double RESIZE_CONSTANT = 150.0;
    public Mat result;
    public Mat mask;
    public Mat image, histogram;
    public String fruitName;
    public static int counter = 0;
    public ContourRecognizer contourRecognizer;
    public ArrayList<ArrayList<Mat>> histograms;

    public String path;

    private MatOfPoint2f toMatOfPoint2f(MatOfPoint m) {
        return new MatOfPoint2f(m.toArray());
    }

    public FruitHistogram(ContourRecognizer contourRecognizer, String fruit) {
        this.fruitName = fruit;
        this.contourRecognizer = contourRecognizer;
        this.histograms = new ArrayList< ArrayList<Mat> >();


    }
    public FruitHistogram() {
        this.histograms = new ArrayList< ArrayList<Mat> >();

    }

//    public void train(ContourRecognizer contourRecognizer, File trainingPath) {
//        this.contourRecognizer = contourRecognizer;
//        if(!trainingPath.exists()) {
//            return ;
//        }
//        for(File image : trainingPath.listFiles()) {
//            this.histograms.add(makeHistogram(image));
//        }
//    }
    public boolean hasTrainingData()
    {
        return histograms != null && !histograms.isEmpty();
    }



    public ArrayList<Mat> makeHistogram(MatOfPoint contur, Mat image) {

        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        List<Mat> imageR = new ArrayList<Mat>();
        List<Mat> imageG = new ArrayList<Mat>();
        List<Mat> imageB = new ArrayList<Mat>();
        List<Mat> normalized = new ArrayList<Mat>();
        Mat histogramR = new Mat();
        Mat histogramG = new Mat();
        Mat histogramB = new Mat();
        Mat normalaizedHistogram = new Mat();
        Mat imageToGray = new Mat();
        Mat imageToHSv = new Mat();


        result.add(contur);
        Mat mask = Mat.zeros(image.height(),image.width(), CvType.CV_8U);
        Imgproc.drawContours(mask,  result, 0, new Scalar(255,255,255), -1);
//        Imgcodecs.imwrite("./output/training/masks/aa" + Integer.toString(counter++) + ".jpg", imageToGray);
//        Imgcodecs.imwrite("./output/training/masks/ab" + Integer.toString(counter++) + ".jpg", normalHist);
//        Imgcodecs.imwrite("./output/training/masks/rgb" + Integer.toString(counter++) + ".jpg", image);
//        Imgcodecs.imwrite("./output/training/masks/gray" + Integer.toString(counter++) + ".jpg", imageToGray);


        Rect roi = Imgproc.boundingRect(contur);
        Mat cropped = new Mat(image, roi);
        Mat cropResized = new Mat();

        double ratio = ((double) roi.height)/((double)roi.width);
        Mat maskCropped = new Mat(mask, roi);
        Mat maskCroppedResized = new Mat(new Size(RESIZE_CONSTANT, ratio*RESIZE_CONSTANT ) ,CvType.CV_8U);
        Imgproc.resize(cropped, cropResized, new Size(RESIZE_CONSTANT, ratio*RESIZE_CONSTANT ));
        Imgproc.resize(maskCropped, maskCroppedResized, new Size(RESIZE_CONSTANT, ratio*RESIZE_CONSTANT ));
//        Imgcodecs.imwrite("./asdf/" + String.valueOf(i) + ".png", cropResized);
        i++;
//
        Imgcodecs.imwrite("./output/training/masks/hsv" + Integer.toString(i++) + ".jpg", cropped);
        Imgcodecs.imwrite("./output/training/masks/a" + Integer.toString(i++) + ".jpg", maskCropped);
        Imgproc.cvtColor(cropResized, imageToGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(cropResized,imageToHSv, Imgproc.COLOR_RGB2HSV);
//        Imgcodecs.imwrite("./output/training/masks/ab" + Integer.toString(counter++) + ".jpg", imageToHSv);

        Mat normalHist = new Mat();
        Imgproc.equalizeHist(imageToGray, normalHist);

//        imageR.add(imageToHSv);
//        imageG.add(imageToHSv);
//        imageB.add(imageToHSv);
        imageR.add(cropResized);
        imageG.add(cropResized);
        imageB.add(cropResized);
        normalized.add(normalHist);
//        normalized.add(imageToGray);
//        images.add(imageToHSv);
        Imgproc.calcHist(imageR, new MatOfInt(0), maskCroppedResized, histogramR, new MatOfInt(25), new MatOfFloat(0,256));
        Imgproc.calcHist(imageG, new MatOfInt(1), maskCroppedResized, histogramG, new MatOfInt(25), new MatOfFloat(0,256));
        Imgproc.calcHist(imageB, new MatOfInt(2), maskCroppedResized, histogramB, new MatOfInt(25), new MatOfFloat(0,256));
        Imgproc.calcHist(normalized, new MatOfInt(0), maskCroppedResized, normalaizedHistogram, new MatOfInt(25), new MatOfFloat(0,256));
//        Imgcodecs.imwrite("./output/training/masks/ab" + Integer.toString(counter++) + ".jpg", histogramR);

        Mat histogramRnormalaize = new Mat();
        Mat histogramGnormalaize = new Mat();
        Mat histogramBnormalaize = new Mat();

//        Core.normalize(histogramR, histogramRnormalaize,0,100,Core.NORM_MINMAX, -1);
//        Core.normalize(histogramG, histogramGnormalaize,0,100,Core.NORM_MINMAX, -1);
//        Core.normalize(histogramB, histogramBnormalaize,0,100,Core.NORM_MINMAX, -1);

        Imgcodecs.imwrite("./output/training/masks/ab" + Integer.toString(counter++) + ".jpg", histogramRnormalaize, new MatOfInt(0,1));
//        Imgproc.equalizeHist(histogram, normalHist);
        ArrayList<Mat> allHistograms = new ArrayList<Mat>();
//        allHistograms.add(histogramRnormalaize);
//        allHistograms.add(histogramGnormalaize);
//        allHistograms.add(histogramBnormalaize);
        allHistograms.add(histogramR);
        allHistograms.add(histogramG);
        allHistograms.add(histogramB);
        allHistograms.add(normalaizedHistogram);
        return allHistograms;
    }

    int i=0;
    public void train(MatOfPoint contur, Mat image) {
        histograms.add(makeHistogram(contur,image));
    }

    public double compareHistograms(List<Mat> histogram1, List<Mat> histogram2 ) {
        int sum = 0;
        for(int i = 0 ; i < histogram1.size(); i++) {
//            System.out.println(Imgproc.compareHist(histogram1.get(i), histogram2.get(i), 0));
            Mat normhistogram1 = new Mat();
            Mat normhistogram2 = new Mat();
            Core.normalize(histogram1.get(i), normhistogram1,0,1,Core.NORM_MINMAX, -1);
            Core.normalize(histogram2.get(i), normhistogram2,0,1,Core.NORM_MINMAX, -1);
//            sum += Imgproc.compareHist(histogram1.get(i), histogram2.get(i), 0);
             sum += Imgproc.compareHist(normhistogram1, normhistogram2, Imgproc.CV_COMP_CHISQR);
//            sum = (int) Math.max(sum, Imgproc.compareHist(normhistogram1, normhistogram2, 1));

        }
        return sum ;
    }

    public List<MatOfPoint> compare(List<MatOfPoint> contures, Mat image, float threshold) {
        int maxComparement = Integer.MAX_VALUE;
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        System.out.println(this.histograms.size());
        for(MatOfPoint contur : contures) {
            List<Mat> tempHistogram = makeHistogram(contur, image);
            for (List<Mat> hist : this.histograms) {
                int value = (int) compareHistograms(tempHistogram, hist);
                if(value >= 0)
                    maxComparement = (int) Math.min(maxComparement, compareHistograms(tempHistogram, hist));

            }

            if (maxComparement <= threshold) {
                result.add(new MatOfPoint(contur));
            }
        }
        System.out.println("Chisloto e " + maxComparement);
//        return maxComparement;
        return result;
    }

//    public float compare(Mat image) {
//        Mat tempHistogram = makeHistogram(image);
//        float maxComparement = 0;
//        for(Mat hist : this.histograms) {
//            maxComparement = (float)Math.max(maxComparement, Imgproc.compareHist(hist, tempHistogram, 0));
//        }
//        return maxComparement;
//
//    }
//    private Mat makeHistogram(Mat image) {
//        List<MatOfPoint> result = contourRecognizer.findContours(image, 0.2);
//        Mat mask = Mat.zeros(image.height(),image.width(), CvType.CV_8U);
//
//        for (int i = 0 ; i < result.size(); i ++) {
//            Imgproc.drawContours(mask, result, i, new Scalar(255,255,255), -1);
//        }
//        List<Mat> images = new ArrayList<Mat>();
//        images.add(image);
//        histogram = new Mat();
//        Imgproc.calcHist(images, new MatOfInt(0,1,2), mask, histogram, new MatOfInt(25), new MatOfFloat(0,256));
////        Imgcodecs.imwrite("./output/training/masks/" + imagePath.getName(), mask);
//        return histogram;
//
//    }
//    private Mat makeHistogram(File imagePath) {
//        File outputFile = new File("./output/training/masks");
//        if (!outputFile.isDirectory()) {
//            outputFile.mkdir();
//        }
//        return makeHistogram(ImageProcessor.openSingleImage(imagePath));
//    }

    public static String matToJson(ArrayList<ArrayList<Mat>> mats){

        Gson gson = new Gson();
        JsonArray arrayOfMat = new JsonArray();
        for(List<Mat> listMat: mats) {

            JsonArray ja = new JsonArray();
            for(Mat mat : listMat) {
                JsonObject obj = new JsonObject();
                if (mat.isContinuous()) {
                    int cols = mat.cols();
                    int rows = mat.rows();
                    int elemSize = (int) mat.elemSize();

                    float[] data = new float[cols * rows];
                    mat.get(0, 0, data);

                    obj.addProperty("rows", mat.rows());
                    obj.addProperty("cols", mat.cols());
                    obj.addProperty("type", mat.type());

                    // We cannot set binary data to a json object, so:
                    // Encoding data byte array to Base64.
//            String dataString = new String(Base64.encodeBase64(data));
//


                    obj.addProperty("data", gson.toJson(data));
                    String json = gson.toJson(obj);
                    ja.add(obj);
                } else {
                    System.out.println("neshto se obarka");
                }
                arrayOfMat.add(ja);
            }

        }
        return gson.toJson(arrayOfMat);
    }

    public static ArrayList<ArrayList<Mat>> matFromJson(String json){
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        ArrayList<ArrayList<Mat>> result = new ArrayList<ArrayList<Mat>>();

        JsonArray jsonArray = parser.parse(json).getAsJsonArray();

        for(JsonElement jsonElement: jsonArray) {
            ArrayList<Mat> mats = new ArrayList<Mat>();
            for(JsonElement je : jsonElement.getAsJsonArray()) {
                JsonObject jsonObject = je.getAsJsonObject();
                int rows = jsonObject.get("rows").getAsInt();
                int cols = jsonObject.get("cols").getAsInt();
                int type = jsonObject.get("type").getAsInt();

                float[] data = gson.fromJson(jsonObject.get("data").getAsString(), float[].class);
                Mat mat = new Mat(rows, cols, type);
                mat.put(0, 0, data);
                mats.add(mat);
            }
            result.add(mats);
        }
        return result;
    }

    public void exportTrainingData(File f) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(f));
            writer.write(matToJson(this.histograms));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadTrainingData(InputStream f) {
        try {
            byte[] data =  new byte[f.available()];
            f.read(data);
            String s = new String(data);
            if (s.length() > 0) {
                this.histograms = matFromJson(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
