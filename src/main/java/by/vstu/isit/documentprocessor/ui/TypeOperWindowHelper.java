package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.controllers.TypeOperEditController;
import by.vstu.isit.documentprocessor.controllers.TypeOperListController;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.experimental.UtilityClass;
import net.rgielen.fxweaver.core.FxWeaver;

import java.util.function.Consumer;

@UtilityClass
public class TypeOperWindowHelper {

    public Parent prepareCreate(FxWeaver fxWeaver) {
        TypeOperEditController controller = fxWeaver.loadController(TypeOperEditController.class);
        controller.initForCreate();
        return fxWeaver.loadView(TypeOperEditController.class);
    }

    public Parent prepareEdit(FxWeaver fxWeaver, TypeOperDto dto) {
        TypeOperEditController controller = fxWeaver.loadController(TypeOperEditController.class);
        controller.initForEdit(dto);
        return fxWeaver.loadView(TypeOperEditController.class);
    }

    public ListWindow prepareList(FxWeaver fxWeaver, Consumer<TypeOperDto> onAdd) {
        TypeOperListController controller = fxWeaver.loadController(TypeOperListController.class);
        controller.initAddCallback(onAdd);
        Parent root = fxWeaver.loadView(TypeOperListController.class);
        return new ListWindow(root, controller);
    }

    public void openCreate(FxWeaver fxWeaver) {
        showModal(prepareCreate(fxWeaver), "Создание типовой операции");
    }

    public void openEdit(FxWeaver fxWeaver, TypeOperDto dto) {
        showModal(prepareEdit(fxWeaver, dto), "Редактирование типовой операции");
    }

    public void openList(FxWeaver fxWeaver, Consumer<TypeOperDto> onAdd) {
        ListWindow window = prepareList(fxWeaver, onAdd);
        showModal(window.root(), "Типовые операции");
        window.controller().initAddCallback(null);
    }

    private void showModal(Parent root, String title) {
        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    public record ListWindow(Parent root, TypeOperListController controller) {
    }
}
