package nl.mikero.turntopassage.frontend.view;

import com.google.inject.Inject;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import nl.mikero.turntopassage.core.exception.TwineRepairFailedException;
import nl.mikero.turntopassage.core.service.TwineService;
import nl.mikero.turntopassage.frontend.control.DropFileChooser;
import nl.mikero.turntopassage.frontend.dialog.ExceptionDialog;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.util.Optional;

public class ApplicationView {
    private final TwineService twineService;

    private static final Image fileImage = new Image("/file.png");
    private static final Image progressImage = new Image("/progress.png");
    private static final Image doneImage = new Image("/done.png");

    private final Alert errorAlert;

    @FXML
    private DropFileChooser dropFileChooser;
    @FXML
    private Button transformButton;

    @Inject
    public ApplicationView(TwineService twineService) {
        this.twineService = twineService;
        this.errorAlert = new Alert(Alert.AlertType.ERROR);
    }

    public Parent getView() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/Application.fxml"));
        fxmlLoader.setController(this);

        try {
            return fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load Application.fxml", e);
        }
    }

    @FXML
    public void initialize() {
        dropFileChooser.setImage(fileImage);

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("HTML Files (*.html, *.htm, *.xhtml)", "*.html", "*.htm", "*.xhtml");
        dropFileChooser.getExtensionFilters().add(extensionFilter);

        dropFileChooser.fileProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null) {
                transformButton.setDisable(false);
            }
        });
    }

    public void onTransformButtonClicked(ActionEvent actionEvent) {
        final File inputFile = dropFileChooser.getFile();
        final File outputFile = new File(getOutputPath(inputFile.getAbsolutePath()));

        if(outputFile.exists()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("File already exists.");
            confirm.setHeaderText(String.format("The file '%s' already exists.", outputFile.getAbsoluteFile()));
            confirm.setContentText("Do you want to overwrite this file?");
            Optional<ButtonType> result = confirm.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.CANCEL) {
                dropFileChooser.openFileChooser();
                return;
            }
        }

        try(InputStream in = new BufferedInputStream(new FileInputStream(inputFile));
            OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            dropFileChooser.setImage(progressImage);
            twineService.transform(in, out);
            dropFileChooser.setImage(doneImage);
        } catch (FileNotFoundException e) {
            String title = String.format("File '%s' could not be found.", inputFile.toString());

            errorAlert.setAlertType(Alert.AlertType.CONFIRMATION);
            errorAlert.setTitle(title);
            errorAlert.setHeaderText(title);
            errorAlert.setContentText(title + " Do you want to select a different file and try again?");
            Optional<ButtonType> result = errorAlert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                dropFileChooser.openFileChooser();
            }
        } catch (TwineRepairFailedException e) {
            String title = String.format("File '%s' could not be repaired.", inputFile.toString());

            errorAlert.setAlertType(Alert.AlertType.ERROR);
            errorAlert.setTitle(title);
            errorAlert.setHeaderText(title);
            errorAlert.setContentText(title + " The file might be in a format that TurnToPassage does not understand. Do you want to select a different file and try again?");
            Optional<ButtonType> result = errorAlert.showAndWait();
            if(result.isPresent() && result.get() == ButtonType.OK) {
                dropFileChooser.openFileChooser();
            }
        } catch (IOException e) {
            ExceptionDialog exceptionDialog = new ExceptionDialog(e);
            exceptionDialog.showAndWait();
        }
    }

    private String getOutputPath(String path) {
        return FilenameUtils.removeExtension(path) + ".epub";
    }
}