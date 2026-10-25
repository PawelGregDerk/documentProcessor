package by.vstu.isit.documentprocessor.ui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class JavaFxAlertService implements AlertService {

    @Override
    public void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).showAndWait();
    }

    @Override
    public void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
    }

    @Override
    public boolean confirm(String title, String header, String content) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle(title);
        confirm.setHeaderText(header);
        confirm.setContentText(content);

        ButtonType yesBtn = new ButtonType("Да", ButtonBar.ButtonData.YES);
        ButtonType cancelBtn = new ButtonType("Отмена", ButtonBar.ButtonData.NO);
        confirm.getButtonTypes().setAll(yesBtn, cancelBtn);

        Optional<ButtonType> response = confirm.showAndWait();
        return response.isPresent() && response.get() == yesBtn;
    }
}
