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
    public Mat result;
    public Mat mask;
    public Mat image, histogram;
    public String fruitName;
    public static int counter = 0;
    public ContourRecognizer contourRecognizer;
    public List<Mat> histograms;

    public String path;

    private MatOfPoint2f toMatOfPoint2f(MatOfPoint m) {
        return new MatOfPoint2f(m.toArray());
    }

    public FruitHistogram(ContourRecognizer contourRecognizer, String fruit) {
        this.fruitName = fruit;
        this.contourRecognizer = contourRecognizer;
        this.histograms = new ArrayList<Mat>();


    }
    public FruitHistogram() {
        this.histograms = new ArrayList<Mat>();

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

    public Mat makeHistogram(MatOfPoint contur, Mat image) {
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        List<Mat> images = new ArrayList<Mat>();
        Mat histogram = new Mat();
        Mat imageToGray = new Mat();
        Mat imageToHSv = new Mat();
        Imgproc.cvtColor(image, imageToGray, Imgproc.COLOR_RGB2GRAY);
        Imgproc.cvtColor(image,imageToHSv, Imgproc.COLOR_RGB2HSV);
        result.add(contur);
        images.add(image);
        images.add(imageToGray);
        images.add(imageToHSv);

        Mat mask = Mat.zeros(image.height(),image.width(), CvType.CV_8U);
        Imgproc.drawContours(mask, result, 0, new Scalar(255,255,255), -1);

//        Imgcodecs.imwrite("./output/training/masks/rgb" + Integer.toString(counter++) + ".jpg", image);
//        Imgcodecs.imwrite("./output/training/masks/gray" + Integer.toString(counter++) + ".jpg", imageToGray);
//        Imgcodecs.imwrite("./output/training/masks/hsv" + Integer.toString(counter++) + ".jpg", imageToHSv);
        Imgcodecs.imwrite("./output/training/masks/ab" + Integer.toString(counter++) + ".jpg", mask);
        Imgproc.calcHist(images, new MatOfInt(2, 0), mask, histogram, new MatOfInt(25, 25), new MatOfFloat(0,256, 0, 256));
//        System.out.println(histogram.dump());
        Mat normalHist = new Mat();
//        Imgproc.equalizeHist(histogram, normalHist);
        return histogram;
    }
    public void train(MatOfPoint contur, Mat image) {
        histograms.add(makeHistogram(contur,image));
    }

    public List<MatOfPoint> compare(List<MatOfPoint> contures, Mat image, float threshold) {
        float maxComparement = 0;
        List<MatOfPoint> result = new ArrayList<MatOfPoint>();
        System.out.println(this.histograms.size());
        for(MatOfPoint contur : contures) {

            Mat tempHistogram = makeHistogram(contur, image);
            for (Mat hist : this.histograms) {
//                maxComparement = (float) Math.max(maxComparement, Imgproc.compareHist(hist, tempHistogram, 0));
                if (Imgproc.compareHist(hist, tempHistogram, 0) >= threshold) {
                    result.add(new MatOfPoint(contur));
                    break;
                }
            }
        }
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

    public static String matToJson(List <Mat> mats){
        JsonArray ja = new JsonArray();
        Gson gson = new Gson();
        for(Mat mat: mats) {
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

        }
        return gson.toJson(ja);
    }

    public static List<Mat> matFromJson(String json){
        Gson gson = new Gson();
        JsonParser parser = new JsonParser();
        List<Mat> mats = new ArrayList<Mat>();
        JsonArray jsonArray = parser.parse(json).getAsJsonArray();
        for(JsonElement jsonElement: jsonArray) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            int rows = jsonObject.get("rows").getAsInt();
            int cols = jsonObject.get("cols").getAsInt();
            int type = jsonObject.get("type").getAsInt();

            float[] data= gson.fromJson(jsonObject.get("data").getAsString(), float[].class);
            Mat mat = new Mat(rows, cols, type);
            mat.put(0, 0, data);
            mats.add(mat);
        }
        return mats;
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
