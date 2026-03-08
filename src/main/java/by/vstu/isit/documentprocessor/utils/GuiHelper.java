package by.vstu.isit.documentprocessor.utils;

import io.vavr.control.Option;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;

import java.util.Objects;

import static by.vstu.isit.documentprocessor.utils.LocalizeHelper.getBundle;
import static by.vstu.isit.documentprocessor.utils.LocalizeHelper.getMessage;

public final class GuiHelper {

    private GuiHelper() {
    }

    public static <T extends Node> T styled(T node, String... styles) {
        for ( var s : styles) {
            if (!node.getStyleClass().contains(s)) {
                node.getStyleClass().add(s);
            }
        }
        return node;
    }

    public static <T> void loadStage(Class<T> controller, FxWeaver fxWeaver, MessageCodes sceneTitle, Stage primaryStage) {
        configureAndShow(primaryStage, controller, fxWeaver, sceneTitle);
    }

    public static <T> void loadStage(Class<T> controller, FxWeaver fxWeaver, Pane pane, MessageCodes sceneTitle) {
        var stage = resolveChildStage(pane).getOrElse(Stage::new);
        configureAndShow(stage, controller, fxWeaver, sceneTitle);
    }

    private static Option<Stage> resolveChildStage(Pane pane) {
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

    private static <T> void configureAndShow(Stage stage, Class<T> controller, FxWeaver fxWeaver, MessageCodes sceneTitle) {
        var scene = new Scene(fxWeaver.loadView(controller, getBundle()), 1280, 1024);
        stage.setScene(scene);
        stage.setTitle(getMessage(sceneTitle));
        stage.setResizable(false);
        addIcon(stage, controller);
        stage.show();
    }

    private static <T> void addIcon(Stage stage, Class<T> tClass) {
        var iconPath = GlobalConsts.getICON_PATH();
        stage.getIcons().add(new Image(Objects.requireNonNull(tClass.getResourceAsStream(iconPath))));
    }
}
