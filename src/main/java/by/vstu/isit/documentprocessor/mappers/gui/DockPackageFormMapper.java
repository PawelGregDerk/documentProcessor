package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class DockPackageFormMapper {
    public DockPackageDto fromGui(
            TextField packageName,
            TextField sborEdNazv,
            TextField pu,
            TextField spu,
            TextField kp,
            TextField fmea,
            TextField vedInstr,
            VBox opersContainer,
            VBox assemblyContainer
    ) {
        return new DockPackageDto(
                null,
                packageName.getText(),
                "path",
                pu.getText(),
                spu.getText(),
                kp.getText(),
                fmea.getText(),
                vedInstr.getText(),
                mapChildren(opersContainer, VBox.class, this::mapOperation),
                mapChildren(assemblyContainer, HBox.class, row -> mapSborEdDto(sborEdNazv, row))
        );
    }

    private SborEdDto mapSborEdDto(TextField nazv, HBox row) {
        return new SborEdDto(null, null, nazv.getText(), text(row, 0));
    }

    private OperDto mapOperation(VBox operBlock) {
        HBox operRow = (HBox) operBlock.getChildren().getFirst();
        VBox funcsContainer = (VBox) operBlock.getChildren().get(1);

        return new OperDto(
                null,
                null,
                text(operRow, 0),
                text(operRow, 1),
                null,
                text(operRow, 2),
                text(operRow, 3),
                text(operRow, 4),
                text(operRow, 5),
                text(operRow, 6),
                mapChildren(funcsContainer, HBox.class, this::mapFunction)
                        .stream()
                        .filter(Objects::nonNull)
                        .toList()
        );
    }

    private FuncDto mapFunction(HBox row) {
        if (!row.getStyleClass().contains("function-data-row")) {
            return null;
        }
        return new FuncDto(
                null,
                text(row, 0),
                null,
                text(row, 1),
                ((CheckBox) row.getChildren().get(2)).isSelected(),
                text(row, 3)
        );
    }

    private <N, R> List<R> mapChildren(VBox container, Class<N> nodeType, Function<N, R> mapper) {
        return container.getChildren().stream()
                .filter(nodeType::isInstance)
                .map(nodeType::cast)
                .map(mapper)
                .toList();
    }

    private String text(HBox box, int idx) {
        return ((TextField) box.getChildren().get(idx)).getText();
    }
}
