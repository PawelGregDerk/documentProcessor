package by.vstu.isit.documentprocessor.mappers.docx;

import by.vstu.isit.documentprocessor.dto.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
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

            TextField oboz = styled(new TextField(), "compact");
            oboz.setText(ed.oboznach());

            Button del = styled(new Button("Удалить"), "delButton");
            del.setOnAction(e -> container.getChildren().remove(row));

            row.getChildren().addAll(oboz, del);
            container.getChildren().add(row);
        }
    }

    private void fillOperations(DockPackageDto dto, VBox container) {
        dto.opers().forEach(o -> container.getChildren().add(createOperation(o)));
    }

    private VBox createOperation(OperDto dto) {

        VBox block = styled(new VBox(5), "operation-block");

        HBox row = styled(new HBox(6), "operation-row");
        row.setAlignment(Pos.CENTER_LEFT);

        row.getChildren().addAll(
                tf(dto.numOper()),
                tf(dto.nomInstr()),
                tf(dto.oborud()),
                tf(dto.ostnasInstr()),
                tf(dto.name()),
                tf(dto.shifr()),
                tf(dto.numZech())
        );

        Button addFunc = styled(new Button("Добавить функцию"), "btn-primary", "btn-small");
        Button delOper = styled(new Button("Удалить"), "btn-danger", "btn-small");

        row.getChildren().addAll(addFunc, delOper);

        VBox funcs = styled(new VBox(4), "funcs-container");

        addFunc.setOnAction(e -> funcs.getChildren().add(createFunction(new FuncDto(null,"", null,"", false, ""))));
        delOper.setOnAction(e -> ((VBox) block.getParent()).getChildren().remove(block));

        dto.funcs().forEach(f -> funcs.getChildren().add(createFunction(f)));

        block.getChildren().addAll(row, funcs);
        return block;
    }

    private HBox createFunction(FuncDto dto) {

        HBox row = styled(new HBox(6), "function-row");
        row.getStyleClass().add("function-data-row");

        TextField name = styled(new TextField(dto.name()), "compact");
        TextField param = styled(new TextField(dto.param()), "no-compact");
        CheckBox prod = new CheckBox();
        prod.setSelected(dto.isProd());
        TextField spec = styled(new TextField(dto.specCharakt()), "compact");

        Button del = styled(new Button("Удалить"), "delButton");
        del.setOnAction(e -> ((VBox) row.getParent()).getChildren().remove(row));

        row.getChildren().addAll(name, param, prod, spec, del);
        return row;
    }

    /* ================= HELPERS ================= */

    private TextField tf(String v) {
        TextField f = styled(new TextField(), "compact");
        f.setText(v);
        return f;
    }

    private void clearAll(VBox ops, VBox sbor) {
        ops.getChildren().clear();
        sbor.getChildren().clear();
    }
}
