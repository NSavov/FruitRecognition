package bg.uni_sofia.fmi.ai.FruitRecognizer;

import ImageProcessor.ContourRecognizer;
import ImageProcessor.ImageProcessor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        FXMLLoader fxmlLoader =new FXMLLoader(getClass().getResource("layout_main.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Fruit Recognizer");
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        Controller controller = fxmlLoader.getController();
        controller.setStage(primaryStage);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
