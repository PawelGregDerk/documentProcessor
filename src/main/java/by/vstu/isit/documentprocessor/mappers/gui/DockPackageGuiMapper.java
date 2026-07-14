package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Component
public class DockPackageGuiMapper {
    public void toGui(
            DockPackageDto dto,
            TextField packageName,
            TextField sborEdNazv,
            TextField pu,
            TextField spu,
            TextField kp,
            TextField fmea,
            TextField vedInstr,
            VBox operationsContainer,
            VBox assemblyUnitsContainer
    ) {

        clearAll(operationsContainer, assemblyUnitsContainer);
        packageName.setText(dto.packageName());
        pu.setText(dto.puName());
        spu.setText(dto.spuName());
        kp.setText(dto.kpName());
        fmea.setText(dto.fmeaName());
        vedInstr.setText(dto.vedIName());

        fillSborEds(dto, sborEdNazv, assemblyUnitsContainer);
        fillOperations(dto, operationsContainer);
    }

    private void fillSborEds(DockPackageDto dto, TextField nazv, VBox container) {

        if (dto.sborEds().isEmpty()) return;

        nazv.setText(dto.sborEds().getFirst().nazv());

        for (SborEdDto ed : dto.sborEds()) {
            HBox row = new HBox(10);
            row.setMaxWidth(Double.MAX_VALUE);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setUserData(ed.id());

         //   TextField oboz = styled(new TextField(), "compact");
         //   oboz.setText(ed.oboznach());
            row.getChildren().add(createStyledField(ed.oboznach(), 140));

            Button del = styled(new Button("Удалить"), "delButton");
            del.setOnAction(e -> {
                VBox parent = (VBox) row.getParent();
                parent.getChildren().remove(row);
                cleanupFunctionHeader(parent);
            });

            row.getChildren().addAll(del);
            container.getChildren().add(row);
        }
    }

    private void fillOperations(DockPackageDto dto, VBox container) {
        dto.opers().forEach(o -> container.getChildren().add(createOperation(o)));
    }

    private VBox createOperation(OperDto dto) {
        VBox block = styled(new VBox(5), "operation-block");
        block.setUserData(dto.id());
        block.setMaxWidth(Double.MAX_VALUE);
        HBox row = styled(new HBox(6), "operation-row");
        row.setMaxWidth(Double.MAX_VALUE);
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
                createStyledArea(dto.numOper(), 80),
                createStyledArea(dto.nomInstr(), 110),
                createStyledArea(dto.oborud(), 120),
                createStyledArea(dto.ostnasInstr(), 140),
                createStyledArea(dto.name(), 160),
                createStyledArea(dto.shifr(), 80),
                createStyledArea(dto.numZech(), 60)
        );

        Button addFunc = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOper = styled(new Button("Удалить"), "btn-danger", "btn-small");

        row.getChildren().addAll(addFunc, delOper);

        VBox funcs = styled(new VBox(4), "funcs-container");
        addFunc.setOnAction(e -> {
            ensureFunctionHeader(funcs);
            funcs.getChildren().add(createFunction(new FuncDto(null, "", null, "", false, "")));
        });
        delOper.setOnAction(e -> ((VBox) block.getParent()).getChildren().remove(block));
        if (!dto.funcs().isEmpty()) {
            ensureFunctionHeader(funcs);
            dto.funcs().forEach(f -> funcs.getChildren().add(createFunction(f)));
        }

        block.getChildren().addAll(row, funcs);
        return block;
    }

    private HBox createFunction(FuncDto dto) {

        HBox row = styled(new HBox(6), "function-row");
        row.getStyleClass().add("function-data-row");
        row.setUserData(dto.id());
        row.setMaxWidth(Double.MAX_VALUE);
        TextArea name = createStyledArea(dto.name(), 140);
        TextArea param = createStyledArea(dto.param(), 140);
        CheckBox prod = new CheckBox();
        prod.setSelected(dto.isProd());
        StackPane checkWrap = new StackPane(prod);
        checkWrap.setAlignment(Pos.CENTER);
        TextArea spec = createStyledArea(dto.specCharakt(), 140);

        Button del = styled(new Button("Удалить"), "delButton");
        del.setOnAction(e -> {
            VBox parent = (VBox) row.getParent();
            parent.getChildren().remove(row);
            cleanupFunctionHeader(parent);
        });

        row.getChildren().addAll(name, param, checkWrap, spec, del);
        setFixedWidth(checkWrap, 70);
        setFixedWidth(del, 90);
        return row;
    }

    /* ================= HELPERS ================= */
//
//    private TextField tf(String v) {
//        TextField f = styled(new TextField(), "compact");
//        f.setText(v);
//        return f;
//    }

    private void ensureFunctionHeader(VBox funcs) {
        boolean hasHeader = funcs.getChildren().stream()
                .anyMatch(n -> n.getStyleClass().contains("function-header-row"));
        if (hasHeader) {
            return;
        }

        HBox header = styled(new HBox(6), "function-header");
        header.getStyleClass().add("function-header-row");
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

    private void cleanupFunctionHeader(VBox funcs) {
        boolean hasDataRows = funcs.getChildren().stream()
                .anyMatch(n -> n.getStyleClass().contains("function-data-row"));
        if (!hasDataRows) {
            funcs.getChildren().removeIf(n -> n.getStyleClass().contains("function-header-row"));
        }
    }

    private void clearAll(VBox ops, VBox sbor) {
        ops.getChildren().clear();
        sbor.getChildren().clear();
    }

    private void setGrow(Region region, double minWidth) {
        region.setMinWidth(minWidth);
        region.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(region, Priority.ALWAYS);
    }

    private void setFixedWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    private void growFunctionHeader(Label name, Label param, Label spec) {
        setGrow(name, 140);
        setGrow(param, 140);
        setGrow(spec, 140);
    }


    private TextField createStyledField(String text, int width) {
        TextField f = styled(new TextField(), "compact");
        if (text != null && !text.isEmpty()) {
            f.setText(text);
        }
        setGrow(f, width);
        return f;
    }

    private TextArea createStyledArea(String text, int width) {
        TextArea a = styled(new TextArea(), "compact");
        a.setPrefRowCount(2);
        a.setWrapText(true);
        if (text != null && !text.isEmpty()) {
            a.setText(text);
        }
        setGrow(a, width);
        return a;
    }
}




