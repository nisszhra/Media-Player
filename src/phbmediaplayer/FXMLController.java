/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package phbmediaplayer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author ZAHRA
 */
public class FXMLController implements Initializable {

    private MediaPlayer mediaPlayer;

    @FXML
    private MediaView mediaView;

    @FXML
    private StackPane sPane;

    @FXML
    private Button playPause;

    @FXML
    private Slider volume;

    @FXML
    private Slider seek;

    @FXML
    private BorderPane bPane;

    @FXML
    private Label nowPlayingLabel;

    List<String> playlist = new ArrayList<>();
    List<String> sourceName = new ArrayList<>();
    static int INDEX, PLAY = 0;
    
    // Volume listener to avoid duplicate listeners
    private final javafx.beans.value.ChangeListener<Number> volumeListener = 
        (observable, oldValue, newValue) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newValue.doubleValue() / 100);
            }
        };
    
    // Supported media file extensions
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".mp3", ".mp4");

    /**
     * Initializes the controller class.
     */
    @FXML
    private void openFiles(ActionEvent event) {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter(
            "Media Files", "*.mp3", "*.mp4", "*.wav", "*.avi", "*.mov", "*.wmv", "*.flv", "*.m4a", "*.aac"
        );
        fc.getExtensionFilters().add(filter);
        List<File> f = fc.showOpenMultipleDialog(null);
        if (f != null && !f.isEmpty()) {
            loadFiles(f);
        }
    }

    @FXML
    private void openFolder(ActionEvent event) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select Media Folder");
        File selectedFolder = dc.showDialog(null);
        
        if (selectedFolder != null) {
            List<File> mediaFiles = new ArrayList<>();
            scanForMediaFiles(selectedFolder, mediaFiles);
            
            if (!mediaFiles.isEmpty()) {
                loadFiles(mediaFiles);
            } else {
                showAlert("No Media Files", "No supported media files found in the selected folder.");
            }
        }
    }

    @FXML
    private void openPlaylist(ActionEvent event) {
        FileChooser fc = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Playlist Files", "*.m3u", "*.txt");
        fc.getExtensionFilters().add(filter);
        File playlistFile = fc.showOpenDialog(null);
        
        if (playlistFile != null) {
            loadPlaylist(playlistFile);
        }
    }

    @FXML
    private void savePlaylist(ActionEvent event) {
        if (playlist.isEmpty()) {
            showAlert("Empty Playlist", "No media files to save in playlist.");
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Save Playlist");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("M3U Playlist", "*.m3u"));
        File file = fc.showSaveDialog(null);
        
        if (file != null) {
            savePlaylistToFile(file);
        }
    }

    private void loadFiles(List<File> files) {
        playlist.clear();
        sourceName.clear();
        for (File file : files) {
            playlist.add(file.toURI().toString());
            sourceName.add(file.getName());
        }
        INDEX = 0;
        playMedia(INDEX);
    }

    private void scanForMediaFiles(File directory, List<File> mediaFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    scanForMediaFiles(file, mediaFiles); // Recursive scan
                } else if (isMediaFile(file)) {
                    mediaFiles.add(file);
                }
            }
        }
    }

    private boolean isMediaFile(File file) {
        String fileName = file.getName().toLowerCase();
        return SUPPORTED_EXTENSIONS.stream().anyMatch(fileName::endsWith);
    }

    private void loadPlaylist(File playlistFile) {
        playlist.clear();
        sourceName.clear();
        
        try (BufferedReader reader = new BufferedReader(new FileReader(playlistFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    File mediaFile = new File(line);
                    if (mediaFile.exists() && isMediaFile(mediaFile)) {
                        playlist.add(mediaFile.toURI().toString());
                        sourceName.add(mediaFile.getName());
                    }
                }
            }
            
            if (!playlist.isEmpty()) {
                INDEX = 0;
                playMedia(INDEX);
            } else {
                showAlert("Invalid Playlist", "No valid media files found in the playlist.");
            }
            
        } catch (IOException e) {
            showAlert("Error", "Failed to load playlist: " + e.getMessage());
        }
    }

    private void savePlaylistToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("#EXTM3U\n");
            for (int i = 0; i < playlist.size(); i++) {
                writer.write("#EXTINF:-1," + sourceName.get(i) + "\n");
                // Convert URI back to file path for saving
                String uri = playlist.get(i);
                if (uri.startsWith("file:/")) {
                    File f = new File(java.net.URI.create(uri));
                    writer.write(f.getAbsolutePath() + "\n");
                } else {
                    writer.write(uri + "\n");
                }
            }
            showAlert("Success", "Playlist saved successfully!");
        } catch (Exception e) {
            showAlert("Error", "Failed to save playlist: " + e.getMessage());
        }
    }

    private void updateNowPlaying() {
        if (INDEX >= 0 && INDEX < sourceName.size()) {
            nowPlayingLabel.setText(sourceName.get(INDEX));
        } else {
            nowPlayingLabel.setText("No media selected");
        }
    }

    private void playMedia(int index) {
        if (index < 0 || index >= playlist.size()) return;
        
        String source = playlist.get(index);
        Media media = new Media(source);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        mediaPlayer = new MediaPlayer(media);
        
        // Setup media view properly
        setupMediaView();
        mediaView.setMediaPlayer(mediaPlayer);

        // Update the now playing display
        updateNowPlaying();

        mediaPlayer.setOnReady(() -> {
            Duration totalDuration = media.getDuration();
            seek.setMax(totalDuration.toSeconds());
            
            // Ensure proper layout after media is ready
            javafx.application.Platform.runLater(() -> {
                bPane.requestLayout();
                sPane.requestLayout();
            });
        });

        // Handle media player errors
        mediaPlayer.setOnError(() -> {
            showAlert("Media Error", "Error playing media: " + mediaPlayer.getError().getMessage());
        });

        volume.setValue(50);
        
        // Remove existing listeners to avoid duplicates
        volume.valueProperty().removeListener(volumeListener);
        volume.valueProperty().addListener(volumeListener);

        mediaPlayer.currentTimeProperty().addListener((ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) -> {
            if (!seek.isValueChanging()) {
                seek.setValue(newValue.toSeconds());
            }
        });

        seek.setOnMousePressed((MouseEvent event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(seek.getValue()));
            }
        });

        seek.setOnMouseDragged((MouseEvent event) -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(seek.getValue()));
            }
        });

        // Auto-play next media when current ends
        mediaPlayer.setOnEndOfMedia(() -> {
            if (INDEX < playlist.size() - 1) {
                forward(null);
            } else {
                stop(null);
            }
        });

        mediaPlayer.play();
        PLAY = 1;
        playPause.setText("Pause");
    }

    @FXML
    private void seekBackward(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().subtract(Duration.seconds(5)));
        }
    }

    @FXML
    private void backward(ActionEvent event) {
        if (INDEX > 0) {
            INDEX--;
            playMedia(INDEX);
        }
    }

    @FXML
    private void stop(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        PLAY = 0;
        playPause.setText("Play");
        
        // Reset UI state
        javafx.application.Platform.runLater(() -> {
            bPane.requestLayout();
            sPane.requestLayout();
        });
    }

    @FXML
    private void pausePlay(ActionEvent event) {
        if (mediaPlayer == null) return;

        if (PLAY == 1) {
            mediaPlayer.pause();
            playPause.setText("Play");
            PLAY = 0;
        } else {
            mediaPlayer.play();
            playPause.setText("Pause");
            PLAY = 1;
        }
    }

    @FXML
    private void forward(ActionEvent event) {
        if (INDEX < playlist.size() - 1) {
            INDEX++;
            playMedia(INDEX);
        }
    }

    @FXML
    private void seekForward(ActionEvent event) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(mediaPlayer.getCurrentTime().add(Duration.seconds(5)));
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupMediaView();
        
        // Initialize the now playing label
        if (nowPlayingLabel != null) {
            nowPlayingLabel.setText("No media selected");
        }
        
        // Add listener for scene changes to handle fullscreen transitions
        bPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getWindow() instanceof javafx.stage.Stage) {
                javafx.stage.Stage stage = (javafx.stage.Stage) newScene.getWindow();
                stage.fullScreenProperty().addListener((observable, oldValue, newValue) -> {
                    handleFullScreenChange(newValue);
                });
            }
        });
    }
    
    private void setupMediaView() {
        // Unbind first to avoid conflicts
        mediaView.fitWidthProperty().unbind();
        mediaView.fitHeightProperty().unbind();
        
        // Bind to parent container
        mediaView.fitWidthProperty().bind(sPane.widthProperty());
        mediaView.fitHeightProperty().bind(sPane.heightProperty());
        mediaView.setPreserveRatio(true);
    }
    
    private void handleFullScreenChange(boolean isFullScreen) {
        // Re-setup media view bindings after fullscreen changes
        javafx.application.Platform.runLater(() -> {
            setupMediaView();
            
            // Force layout update
            bPane.requestLayout();
            sPane.requestLayout();
            
            // Refresh media view
            if (mediaPlayer != null) {
                mediaView.setMediaPlayer(null);
                mediaView.setMediaPlayer(mediaPlayer);
            }
        });
    }    
}