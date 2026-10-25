package by.vstu.isit.documentprocessor.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import lombok.experimental.UtilityClass;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@UtilityClass
public class FunctionHeaderManager {

    private static final String HEADER_STYLE = "function-header-row";
    private static final String DATA_STYLE = "function-data-row";

    public void ensureHeader(VBox funcs) {
        boolean hasHeader = funcs.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains(HEADER_STYLE));
        if (hasHeader) {
            return;
        }

        HBox header = styled(new HBox(6), "function-header");
        header.getStyleClass().add(HEADER_STYLE);
        header.setMaxWidth(Double.MAX_VALUE);
        header.getChildren().addAll(
                styled(new Label("Описание функции"), "table-header"),
                styled(new Label("Параметры / Требования"), "table-header"),
                styled(new Label("Продукт"), "table-header"),
                styled(new Label("Спец. характеристики"), "table-header")
        );
        Region buttonGap = new Region();
        header.getChildren().add(buttonGap);
        growFunctionHeader(
                (Label) header.getChildren().get(0),
                (Label) header.getChildren().get(1),
                (Label) header.getChildren().get(3)
        );
        setFixedWidth((Region) header.getChildren().get(2), 70);
        setFixedWidth(buttonGap, 90);
        funcs.getChildren().add(0, header);
    }

    public void cleanupIfEmpty(VBox funcs) {
        boolean hasDataRows = funcs.getChildren().stream()
                .anyMatch(node -> node.getStyleClass().contains(DATA_STYLE));
        if (!hasDataRows) {
            funcs.getChildren().removeIf(node -> node.getStyleClass().contains(HEADER_STYLE));
        }
    }
}
