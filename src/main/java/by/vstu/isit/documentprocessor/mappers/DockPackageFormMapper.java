package by.vstu.isit.documentprocessor.mappers;

import by.vstu.isit.documentprocessor.controllers.form.DockPackageFormState;
import by.vstu.isit.documentprocessor.controllers.form.FuncFormState;
import by.vstu.isit.documentprocessor.controllers.form.OperFormState;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class DockPackageFormMapper {
    public DockPackageFormState fromGui(
            TextField packageName,
            TextField pu,
            TextField spu,
            TextField kp,
            TextField fmea,
            TextField vedInstr,
            TextField extra,
            VBox operationsContainer
    ) {
        List<OperFormState> operations = new ArrayList<>();

        for (Node node : operationsContainer.getChildren()) {
            VBox operBlock = (VBox) node;
            HBox operRow = (HBox) operBlock.getChildren().get(0);
            VBox funcsBox = (VBox) operBlock.getChildren().get(1);

            List<FuncFormState> funcs = new ArrayList<>();

            for (Node fn : funcsBox.getChildren()) {
                if (!fn.getStyleClass().contains("function-data-row")) continue;

                HBox fr = (HBox) fn;
                funcs.add(new FuncFormState(
                        text(fr, 0),
                        text(fr, 1),
                        ((CheckBox) fr.getChildren().get(2)).isSelected(),
                        text(fr, 3)
                ));
            }

            operations.add(new OperFormState(
                    text(operRow, 0),
                    text(operRow, 1),
                    text(operRow, 2),
                    text(operRow, 3),
                    text(operRow, 4),
                    text(operRow, 5),
                    text(operRow, 6),
                    extra.getText(),
                    funcs
            ));
        }

        return new DockPackageFormState(
                packageName.getText(),
                pu.getText(),
                spu.getText(),
                kp.getText(),
                fmea.getText(),
                vedInstr.getText(),
                operations
        );
    }

    private String text(HBox box, int idx) {
        return ((TextField) box.getChildren().get(idx)).getText();
    }
}

