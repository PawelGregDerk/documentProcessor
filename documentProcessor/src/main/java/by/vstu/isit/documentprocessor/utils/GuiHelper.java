package by.vstu.isit.documentprocessor.utils;

import io.vavr.control.Option;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import net.rgielen.fxweaver.core.FxWeaver;

import java.util.Objects;

import static by.vstu.isit.documentprocessor.utils.LocalizeHelper.*;

@UtilityClass
public class GuiHelper {
    private static final double WINDOW_SIZE_RATIO = 0.9;

    public <T extends Node> T styled(T node, String... styles) {
        for ( var s : styles) {
            if (!node.getStyleClass().contains(s)) {
                node.getStyleClass().add(s);
            }
        }
        return node;
    }

    public <T> void loadStage(Class<T> controller, FxWeaver fxWeaver, MessageCodes sceneTitle, Stage primaryStage) {
        configureAndShow(primaryStage, controller, fxWeaver, sceneTitle);
    }

    public <T> void loadStage(Class<T> controller, FxWeaver fxWeaver, Pane pane, MessageCodes sceneTitle) {
        var stage = resolveChildStage(pane).getOrElse(Stage::new);
        configureAndShow(stage, controller, fxWeaver, sceneTitle);
    }

    private Option<Stage> resolveChildStage(Pane pane) {
        return Option.of(pane)
                .map(Pane::getScene)
                .map(Scene::getWindow)
                .filter(Stage.class::isInstance)
                .map(Stage.class::cast)
                .map(parentStage -> {
                    parentStage.hide();
                    Stage childStage = new Stage();
                    childStage.initOwner(parentStage);
                    childStage.setOnHidden(e -> parentStage.show());
                    return childStage;
                });
    }

    private <T> void configureAndShow(Stage stage, Class<T> controller, FxWeaver fxWeaver, MessageCodes sceneTitle) {
        var scene = createScreenRatioScene(fxWeaver.loadView(controller, getBundle()));
        stage.setScene(scene);
        stage.setTitle(getMessage(sceneTitle));
        stage.setResizable(false);
        addIcon(stage, controller);
        stage.show();
    }

    public Scene createScreenRatioScene(Parent root) {
        var bounds = Screen.getPrimary().getVisualBounds();
        return new Scene(
                root,
                bounds.getWidth() * WINDOW_SIZE_RATIO,
                bounds.getHeight() * WINDOW_SIZE_RATIO
        );
    }

    private <T> void addIcon(Stage stage, Class<T> tClass) {
        var iconPath = GlobalConsts.getICON_PATH();
        stage.getIcons().add(new Image(Objects.requireNonNull(tClass.getResourceAsStream(iconPath))));
    }
}
