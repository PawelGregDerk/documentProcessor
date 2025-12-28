package by.vstu.isit.documentprocessor.utils;

import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import net.rgielen.fxweaver.core.FxWeaver;

import java.util.Objects;

import static by.vstu.isit.documentprocessor.utils.LocalizeHelper.*;

@UtilityClass
public class ResourceHelper {
    public <T> void addIcon(Stage stage, Class<T> tClass) {
        stage.getIcons().add(new Image(Objects.requireNonNull(tClass.getResourceAsStream("/images/logo.png"))));
    }

    public <T> void loadStage(Class<T> controller, FxWeaver fxWeaver, Pane pane, MessageCodes sceneTitle) {
        Stage previousStage = (Stage) pane.getScene().getWindow(); // или другой FXML-элемент
        previousStage.hide();
        Stage stage = new Stage();
        stage.setScene(new Scene(fxWeaver.loadView(controller, getBundle()), 1152, 824));
        addIcon(stage, controller);
        stage.setTitle(getMessage(sceneTitle));
        stage.setResizable(false);
        stage.initOwner(previousStage);
        stage.setOnHidden(e -> previousStage.show());
        stage.show();
    }

    public <T extends Node> T styled(T node, String... styles) {
        for (String s : styles) {
            if (!node.getStyleClass().contains(s)) {
                node.getStyleClass().add(s);
            }
        }
        return node;
    }
}
