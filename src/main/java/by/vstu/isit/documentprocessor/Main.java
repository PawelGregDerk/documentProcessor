package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.controllers.DocPackageController;
import by.vstu.isit.documentprocessor.controllers.MainController;
import by.vstu.isit.documentprocessor.utils.MessageCodes;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.MAIN_SCENE;
import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@SpringBootApplication
public class Main extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = SpringApplication.run(Main.class);
    }

    @Override
    public void start(Stage primaryStage) {
        var fxWeaver = springContext.getBean(FxWeaver.class);
        loadStage(MainController.class, fxWeaver, MAIN_SCENE, primaryStage);
    }

    @Override
    public void stop() {
        springContext.close();
    }

    static void main(String[] args) {
        launch(Main.class, args);
    }
}
