package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.utils.GlobalConsts;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.Objects;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.*;
import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("main-view.fxml")
@RequiredArgsConstructor
public class MainController {
    private final FxWeaver fxWeaver;
    private final DockPackageGuiMapper guiMapper;
    private final DocpackageService docpackageService;

    @FXML
    private VBox mainVBox;

    public void createDocPackage() {
        loadStage(DocPackageTestCreateController.class, fxWeaver, mainVBox, NEW_DOC_PACKAGE);
   }

    @FXML
    public void loadFromDB() {
        Stage parentStage = (Stage) mainVBox.getScene().getWindow();
        parentStage.hide();

        // Создаем окно с прогресс-баром
        Stage progressStage = new Stage();
        progressStage.initOwner(parentStage);
        progressStage.setTitle("Загрузка...");

        ProgressBar progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        VBox progressBox = new VBox(20, new javafx.scene.control.Label("Загрузка списка пакетов..."), progressBar);
        progressBox.setAlignment(javafx.geometry.Pos.CENTER);
        progressBox.setPadding(new Insets(20));

        progressStage.setScene(new Scene(progressBox, 400, 150));
        progressStage.setResizable(false);
        progressStage.show();

        Task<Pane> loadTask = getPaneTask(progressStage, parentStage);

        new Thread(loadTask).start();
    }

    private Task<Pane> getPaneTask(Stage progressStage, Stage parentStage) {
        Task<Pane> loadTask = new Task<>() {
            @Override
            protected Pane call() {
                // Загружаем контроллер и view в фоновом потоке
                PackageListController controller = fxWeaver.loadController(PackageListController.class);
                return fxWeaver.loadView(PackageListController.class);
            }
        };

        loadTask.setOnSucceeded(e -> {
            progressStage.close();
            Pane view = loadTask.getValue();

            Stage childStage = new Stage();
            childStage.initOwner(parentStage);
            childStage.setScene(new Scene(view, 1280, 1024));
            childStage.setTitle("Список пакетов документов");
            childStage.setResizable(false);
            addIcon(childStage, PackageListController.class);

            // Добавляем обработчик только для закрытия крестиком
            childStage.setOnCloseRequest(ev -> parentStage.show());

            childStage.show();
        });
        return loadTask;
    }

    private <T> void openDocPackageForm(Class<T> controllerClass, String title) {
        Stage parentStage = (Stage) mainVBox.getScene().getWindow();
        parentStage.hide();

        T controller = fxWeaver.loadController(controllerClass);
        Pane view = fxWeaver.loadView(controllerClass);

        Stage childStage = new Stage();
        childStage.initOwner(parentStage);
        childStage.setOnHidden(e -> parentStage.show());
        childStage.setScene(new Scene(view, 1280, 1024));
        childStage.setTitle(title);
        childStage.setResizable(false);
        addIcon(childStage, controllerClass);
        childStage.show();
    }

    private <T> void addIcon(Stage stage, Class<T> tClass) {
        var iconPath = GlobalConsts.getICON_PATH();
        stage.getIcons().add(new Image(Objects.requireNonNull(tClass.getResourceAsStream(iconPath))));
    }
}
