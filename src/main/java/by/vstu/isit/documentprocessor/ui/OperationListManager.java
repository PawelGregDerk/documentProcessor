package by.vstu.isit.documentprocessor.ui;

import by.vstu.isit.documentprocessor.dto.TypeOperDto;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class OperationListManager {

    private final VBox container;
    private final IntConsumer openTypeOperList;

    public OperationListManager(VBox container, IntConsumer openTypeOperList) {
        this.container = container;
        this.openTypeOperList = openTypeOperList;
    }

    public void addOperation() {
        insertAt(blockCount(), null);
    }

    public void insertAt(int index, TypeOperDto typeOper) {
        var blocks = getBlocks();
        VBox newBlock = typeOper != null
                ? OperationBlockFactory.createOperationBlock(typeOper, this::remove)
                : OperationBlockFactory.createEmptyOperationBlock(this::remove);
        blocks.add(Math.max(0, Math.min(index, blocks.size())), newBlock);
        container.getChildren().clear();
        container.getChildren().addAll(blocks);
        rebuildSeparators();
    }

    public void remove(Node block) {
        container.getChildren().remove(block);
        rebuildSeparators();
    }

    public int blockCount() {
        return getBlocks().size();
    }

    public void rebuildSeparators() {
        var blocks = getBlocks();
        container.getChildren().clear();
        for (int i = 0; i < blocks.size(); i++) {
            container.getChildren().add(createSeparator(i));
            container.getChildren().add(blocks.get(i));
        }
    }

    public void wrapDeleteHandlers() {
        for (var block : getBlocks()) {
            VBox vb = (VBox) block;
            if (vb.getChildren().isEmpty()) {
                continue;
            }
            Node firstChild = vb.getChildren().get(0);
            if (!(firstChild instanceof HBox operRow)) {
                continue;
            }
            for (var child : operRow.getChildren()) {
                if (child instanceof Button btn && "Удалить".equals(btn.getText())) {
                    btn.setOnAction(e -> remove(block));
                    break;
                }
            }
        }
    }

    private HBox createSeparator(int insertIndex) {
        HBox sep = new HBox();
        sep.setAlignment(Pos.CENTER);
        sep.setStyle("-fx-border-color: #ddd; -fx-border-width: 1 0 0 0; -fx-padding: 2;");

        Button plusBtn = new Button("Добавить операцию");
        plusBtn.getStyleClass().addAll("btn-small", "btn-add");

        ContextMenu menu = new ContextMenu();
        menu.getStyleClass().add("btn-add");
        MenuItem regularItem = new MenuItem("Обычная операция");
        regularItem.setOnAction(e -> {
            menu.hide();
            insertAt(insertIndex, null);
        });
        MenuItem typeItem = new MenuItem("Типовая операция");
        typeItem.setOnAction(e -> {
            menu.hide();
            openTypeOperList.accept(insertIndex);
        });
        menu.getItems().addAll(regularItem, typeItem);

        plusBtn.setOnAction(e -> {
            double width = plusBtn.getWidth();
            menu.setStyle("-fx-pref-width: " + width + "px; -fx-min-width: " + width + "px;");
            menu.show(plusBtn, Side.BOTTOM, 0, 0);
        });

        sep.getChildren().add(plusBtn);
        return sep;
    }

    private List<Node> getBlocks() {
        return container.getChildren().stream()
                .filter(node -> node.getStyleClass().contains("operation-block"))
                .collect(Collectors.toList());
    }
}
