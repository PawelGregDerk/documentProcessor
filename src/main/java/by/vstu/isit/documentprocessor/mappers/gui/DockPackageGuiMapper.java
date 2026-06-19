package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
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
            row.setAlignment(Pos.CENTER_LEFT);
            row.setUserData(ed.id());

            TextField oboz = styled(new TextField(), "compact");
            oboz.setText(ed.oboznach());

            Button del = styled(new Button("Удалить"), "delButton");
            del.setOnAction(e -> {
                VBox parent = (VBox) row.getParent();
                parent.getChildren().remove(row);
                cleanupFunctionHeader(parent);
            });

            row.getChildren().addAll(oboz, del);
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
                createStyledField(dto.numOper(), 80),
                createStyledField(dto.nomInstr(), 110),
                createStyledField(dto.oborud(), 120),
                createStyledField(dto.ostnasInstr(), 140),
                createStyledField(dto.name(), 160),
                createStyledField(dto.shifr(), 80),
                createStyledField(dto.numZech(), 60)
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
        TextField name = createStyledField(dto.name(), 140);
        TextField param = param = createStyledField(dto.param(), 140);
        CheckBox prod = new CheckBox();
        prod.setSelected(dto.isProd());
        prod.setPrefWidth(20);
        TextField spec = createStyledField(dto.specCharakt(), 140);

        Button del = styled(new Button("Удалить"), "delButton");
        del.setOnAction(e -> {
            VBox parent = (VBox) row.getParent();
            parent.getChildren().remove(row);
            cleanupFunctionHeader(parent);
        });

        row.getChildren().addAll(name, param, prod, spec, del);
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
        header.getChildren().addAll(
                styled(new Label("Описание функции"), "table-header"),
                styled(new Label("Параметры / Требования"), "table-header"),
                styled(new Label("Продукт"), "table-header"),
                styled(new Label("Спец. характеристики"), "table-header")
        );
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


    private TextField createStyledField(String text, int width) {
        TextField f = styled(new TextField(), "compact");
        if (text != null && !text.isEmpty()) {
            f.setText(text);
        }
        setGrow(f, width);
        return f;
    }
}




