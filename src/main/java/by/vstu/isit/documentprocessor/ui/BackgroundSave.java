package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.function.Consumer;

public final class BackgroundSave {

    private BackgroundSave() {
    }

    public static void run(Window owner, Action action, Runnable onSuccess, Consumer<Throwable> onError) {
        Stage progress = showProgress(owner);
        Thread thread = new Thread(() -> {
            try {
                action.run();
                Platform.runLater(() -> {
                    progress.close();
                    onSuccess.run();
                });
            } catch (Throwable error) {
                Platform.runLater(() -> {
                    progress.close();
                    onError.accept(error);
                });
            }
        }, "document-save");
        thread.setDaemon(true);
        thread.start();
    }

    private static Stage showProgress(Window owner) {
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(48, 48);
        Label label = new Label("Сохранение документов...");
        VBox box = new VBox(12, indicator, label);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(20));

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);
        stage.setTitle("Сохранение");
        stage.setScene(new Scene(box, 320, 140));
        stage.setOnCloseRequest(event -> event.consume());
        stage.show();
        return stage;
    }

    @FunctionalInterface
    public interface Action {
        void run() throws Exception;
    }
}
