package by.vstu.isit.documentprocessor.mappers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DockPackageFormMapper {
    public DockPackageDto fromGui(
            TextField packageName,
            TextField pu,
            TextField spu,
            TextField kp,
            TextField fmea,
            TextField vedInstr,
            TextField extra,
            VBox operationsContainer
    ) {
        return new DockPackageDto(
                packageName.getText(),
                pu.getText(),
                spu.getText(),
                kp.getText(),
                fmea.getText(),
                vedInstr.getText(),
                extra.getText(),
                mapOperations(operationsContainer)
        );
    }

    private List<OperDto> mapOperations(VBox container) {
        List<OperDto> result = new ArrayList<>();
        for (var node : container.getChildren()) {
            result.add(mapOperation((VBox) node));
        }
        return result;
    }

    private OperDto mapOperation(VBox operBlock) {
        HBox operRow = (HBox) operBlock.getChildren().get(0);
        VBox funcsBox = (VBox) operBlock.getChildren().get(1);
        return new OperDto(
                text(operRow, 0),
                text(operRow, 1),
                text(operRow, 2),
                text(operRow, 3),
                text(operRow, 4),
                text(operRow, 5),
                text(operRow, 6),
                mapFunctions(funcsBox)
        );
    }

    private List<FuncDto> mapFunctions(VBox funcsBox) {
        List<FuncDto> result = new ArrayList<>();
        for (var node : funcsBox.getChildren()) {
            if (node.getStyleClass().contains("function-data-row")) {
                result.add(mapFunction((HBox) node));
            }

        }
        return result;
    }

    private FuncDto mapFunction(HBox row) {
        return new FuncDto(
                text(row, 0),
                text(row, 1),
                ((CheckBox) row.getChildren().get(2)).isSelected(),
                text(row, 3)
        );
    }

    private String text(HBox box, int idx) {
        return ((TextField) box.getChildren().get(idx)).getText();
    }
}
