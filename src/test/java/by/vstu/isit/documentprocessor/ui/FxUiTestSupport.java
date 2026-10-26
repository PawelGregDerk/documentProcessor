package by.vstu.isit.documentprocessor.ui;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.robot.Robot;
import javafx.stage.Stage;

import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

final class FxUiTestSupport {

    private FxUiTestSupport() {
    }

    static void startToolkit() {
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException ignored) {
            // already initialized by another test class
        }
        runFx(() -> {
            Platform.setImplicitExit(false);
            return null;
        });
    }

    static Parent load(String resource, Object controller) {
        return runFx(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(FxUiTestSupport.class.getResource(resource));
                loader.setResources(ResourceBundle.getBundle("i18n.messages"));
                loader.setControllerFactory(type -> {
                    if (type.isInstance(controller)) {
                        return controller;
                    }
                    throw new IllegalStateException("Unexpected controller type: " + type);
                });
                return loader.load();
            } catch (Exception e) {
                throw new IllegalStateException("Не удалось загрузить FXML: " + resource, e);
            }
        });
    }

    static Stage show(Parent root) {
        return runFx(() -> {
            Stage stage = new Stage();
            stage.setScene(new Scene(root, 1400, 1000));
            stage.setX(50);
            stage.setY(50);
            stage.setAlwaysOnTop(true);
            stage.show();
            stage.toFront();
            root.applyCss();
            root.layout();
            return stage;
        });
    }

    static void click(Node node) throws Exception {
        Point2D point = runFx(() -> {
            Stage stage = (Stage) node.getScene().getWindow();
            stage.toFront();
            stage.requestFocus();
            Bounds bounds = node.getBoundsInLocal();
            return node.localToScreen(bounds.getCenterX(), bounds.getCenterY());
        });

        runFx(() -> {
            Robot robot = new Robot();
            robot.mouseMove(point);
            robot.mouseClick(MouseButton.PRIMARY);
            return null;
        });

        // даём JavaFX доставить событие и перестроить layout
        Thread.sleep(500);
        runFx(() -> null);
    }

    static Parent root(Node node) {
        return node.getScene().getRoot();
    }

    static Button button(Parent root, String text) {
        return runFx(() -> root.lookupAll(".button").stream()
                .filter(Button.class::isInstance)
                .map(Button.class::cast)
                .filter(button -> text.equals(button.getText()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Кнопка не найдена: " + text)));
    }

    @SuppressWarnings("unchecked")
    static <T extends Node> T node(Parent root, String id) {
        return runFx(() -> {
            Node node = root.lookup("#" + id);
            if (node == null) {
                throw new AssertionError("Узел не найден: #" + id);
            }
            return (T) node;
        });
    }

    static void close(Stage stage) {
        if (stage != null) {
            runFx(() -> {
                stage.close();
                return null;
            });
        }
    }

    private static <T> T runFx(Supplier<T> supplier) {
        CountDownLatch latch = new CountDownLatch(1);
        Object[] result = new Object[1];
        Throwable[] error = new Throwable[1];
        Platform.runLater(() -> {
            try {
                result[0] = supplier.get();
            } catch (Throwable e) {
                error[0] = e;
            } finally {
                latch.countDown();
            }
        });
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("JavaFX operation timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        if (error[0] != null) {
            throw new IllegalStateException(error[0]);
        }
        return (T) result[0];
    }

    private static void runFx(Runnable runnable) {
        runFx(() -> {
            runnable.run();
            return null;
        });
    }
}
