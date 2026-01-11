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
        List<OperDto> operations = new ArrayList<>();

        for (var node : operationsContainer.getChildren()) {
            VBox operBlock = (VBox) node;
            HBox operRow = (HBox) operBlock.getChildren().get(0);
            VBox funcsBox = (VBox) operBlock.getChildren().get(1);

            List<FuncDto> funcs = new ArrayList<>();
            for (var fn : funcsBox.getChildren()) {
                if (!fn.getStyleClass().contains("function-data-row")) {
                    continue;
                }

                HBox fr = (HBox) fn;
                funcs.add(new FuncDto(
                        text(fr, 0),
                        text(fr, 1),
                        ((CheckBox) fr.getChildren().get(2)).isSelected(),
                        text(fr, 3)
                ));
            }

            operations.add(new OperDto(
                    text(operRow, 0),
                    text(operRow, 1),
                    text(operRow, 2),
                    text(operRow, 3),
                    text(operRow, 4),
                    text(operRow, 5),
                    text(operRow, 6),
                    funcs
            ));
        }

        return new DockPackageDto(
                packageName.getText(),
                pu.getText(),
                spu.getText(),
                kp.getText(),
                fmea.getText(),
                vedInstr.getText(),
                extra.getText(),
                operations
        );
    }

    private String text(HBox box, int idx) {
        return ((TextField) box.getChildren().get(idx)).getText();
    }
}

