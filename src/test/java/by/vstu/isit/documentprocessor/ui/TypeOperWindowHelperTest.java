package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.controllers.TypeOperEditController;
import by.vstu.isit.documentprocessor.controllers.TypeOperListController;
import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import by.vstu.isit.documentprocessor.services.db.interfaces.TypeOperService;
import javafx.scene.Parent;
import net.rgielen.fxweaver.core.FxWeaver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TypeOperWindowHelperTest {

    @Mock
    private TypeOperService typeOperService;
    @Mock
    private FxWeaver fxWeaver;

    @Test
    @DisplayName("prepareCreate: initializes controller for create and loads view")
    void prepareCreate_initializesCreateMode() {
        TypeOperEditController controller = spy(new TypeOperEditController(typeOperService));
        Parent root = org.mockito.Mockito.mock(Parent.class);
        when(fxWeaver.loadController(TypeOperEditController.class)).thenReturn(controller);
        when(fxWeaver.loadView(TypeOperEditController.class)).thenReturn(root);

        Parent result = TypeOperWindowHelper.prepareCreate(fxWeaver);

        assertThat(result).isSameAs(root);
        verify(controller).initForCreate();
    }

    @Test
    @DisplayName("prepareEdit: passes DTO into controller and loads view")
    void prepareEdit_initializesEditMode() {
        TypeOperDto dto = new TypeOperDto(
                1L, "001", "15-01", "Станок", "Оснастка", "Операция", "Ш", "15", List.of());
        TypeOperEditController controller = spy(new TypeOperEditController(typeOperService));
        Parent root = org.mockito.Mockito.mock(Parent.class);
        when(fxWeaver.loadController(TypeOperEditController.class)).thenReturn(controller);
        when(fxWeaver.loadView(TypeOperEditController.class)).thenReturn(root);

        Parent result = TypeOperWindowHelper.prepareEdit(fxWeaver, dto);

        assertThat(result).isSameAs(root);
        verify(controller).initForEdit(dto);
    }

    @Test
    @DisplayName("prepareList: passes add callback and returns controller with view")
    void prepareList_setsAddCallback() {
        Consumer<TypeOperDto> callback = dto -> {};
        TypeOperListController controller = spy(new TypeOperListController(typeOperService, fxWeaver));
        Parent root = org.mockito.Mockito.mock(Parent.class);
        when(fxWeaver.loadController(TypeOperListController.class)).thenReturn(controller);
        when(fxWeaver.loadView(TypeOperListController.class)).thenReturn(root);

        TypeOperWindowHelper.ListWindow window = TypeOperWindowHelper.prepareList(fxWeaver, callback);

        assertThat(window.root()).isSameAs(root);
        assertThat(window.controller()).isSameAs(controller);
        verify(controller).initAddCallback(callback);
    }
}
