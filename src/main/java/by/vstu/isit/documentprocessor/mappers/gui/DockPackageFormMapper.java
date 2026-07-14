package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.utils.FileUtils;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Component
public class DockPackageFormMapper {
    public DockPackageDto fromGui(
            Long packageId,
            TextField packageName,
            String packagePath,
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
                packageId,
                FileUtils.sanitize(packageName.getText().trim()),
                FileUtils.sanitize(packagePath),
                FileUtils.sanitize(pu.getText()),
                FileUtils.sanitize(spu.getText()),
                FileUtils.sanitize(kp.getText()),
                FileUtils.sanitize(fmea.getText()),
                FileUtils.sanitize(vedInstr.getText()),
                mapChildren(opersContainer, VBox.class, this::mapOperation),
                mapChildren(assemblyContainer, HBox.class, row -> mapSborEdDto(sborEdNazv, row))
        );
    }

    private SborEdDto mapSborEdDto(TextField nazv, HBox row) {
        Long id = (Long) row.getUserData();
        return new SborEdDto(id, null, FileUtils.sanitize(nazv.getText()), text(row, 0));
    }

    private OperDto mapOperation(VBox operBlock) {
        Long id = (Long) operBlock.getUserData();
        HBox operRow = (HBox) operBlock.getChildren().getFirst();
        VBox funcsContainer = (VBox) operBlock.getChildren().get(1);
        return new OperDto(
                id,
                null,
                FileUtils.sanitize(text(operRow, 0)),
                text(operRow, 1),
                text(operRow, 2),
                text(operRow, 3),
                FileUtils.sanitize(text(operRow, 4)),
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
        Long id = (Long) row.getUserData();
        return new FuncDto(
                id,
                text(row, 0),
                null,
                text(row, 1),
                isProdSelected(row.getChildren().get(2)),
                text(row, 3)
        );
    }

    private boolean isProdSelected(javafx.scene.Node node) {
        if (node instanceof StackPane sp && sp.getChildren().getFirst() instanceof CheckBox cb) {
            return cb.isSelected();
        }
        if (node instanceof CheckBox cb) {
            return cb.isSelected();
        }
        return false;
    }

    private <N, R> List<R> mapChildren(VBox container, Class<N> nodeType, Function<N, R> mapper) {
        return container.getChildren().stream()
                .filter(nodeType::isInstance)
                .map(nodeType::cast)
                .map(mapper)
                .toList();
    }

    private String text(HBox box, int idx) {
        var node = box.getChildren().get(idx);
        if (node instanceof TextField tf) return tf.getText();
        if (node instanceof TextArea ta) return ta.getText();
        return "";
    }
}
