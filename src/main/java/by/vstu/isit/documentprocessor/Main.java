package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.controllers.MainController;
import by.vstu.isit.documentprocessor.utils.MessageCodes;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static by.vstu.isit.documentprocessor.utils.LocalizeHelper.*;
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
        primaryStage.setTitle(getMessage(MessageCodes.MAIN_SCENE));
        var fxWeaver = springContext.getBean(FxWeaver.class);
        var scene = new Scene(fxWeaver.loadView(MainController.class, getBundle()), 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        addIcon(primaryStage, this.getClass());
        primaryStage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }

    static void main(String[] args) {
        launch(Main.class, args);
    }
}
