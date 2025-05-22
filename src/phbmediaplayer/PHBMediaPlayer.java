/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package phbmediaplayer;

import javafx.application.Application;
import static javafx.application.Application.launch;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
/**
 *
 * 
 * @author ZAHRA
 */
public class PHBMediaPlayer extends Application {
    @Override
    public void start(Stage stage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/phbmediaplayer/FXML.fxml"));
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("PHB Media Player");
            scene.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2) {
                    stage.setFullScreen(true);
                }
            });
            stage.show(); 
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
