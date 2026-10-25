package by.vstu.isit.documentprocessor.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import lombok.experimental.UtilityClass;

import java.util.function.Consumer;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@UtilityClass
public class AssemblyUnitRowFactory {

    public HBox createRow(String value, Long userData, Consumer<Node> onDelete) {
        HBox row = new HBox(10);
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setUserData(userData);

        TextField codeField = styled(new TextField(), "compact");
        codeField.setPromptText("Обозначение сборочной единицы");
        if (value != null) {
            codeField.setText(value);
        }
        codeField.prefWidthProperty().bind(row.widthProperty().multiply(0.75));
        HBox.setHgrow(codeField, Priority.ALWAYS);

        Button deleteButton = styled(new Button("Удалить"), "delButton");
        deleteButton.setOnAction(e -> onDelete.accept(row));

        row.getChildren().addAll(codeField, deleteButton);
        return row;
    }
}
