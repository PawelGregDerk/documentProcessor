package by.vstu.isit.documentprocessor.mappers.gui;

import by.vstu.isit.documentprocessor.dto.*;
import by.vstu.isit.documentprocessor.ui.AssemblyUnitRowFactory;
import by.vstu.isit.documentprocessor.ui.OperationBlockFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.springframework.stereotype.Component;

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
            container.getChildren().add(AssemblyUnitRowFactory.createRow(
                    ed.oboznach(), ed.id(), row -> container.getChildren().remove(row)));
        }
    }

    private void fillOperations(DockPackageDto dto, VBox container) {
        dto.opers().forEach(oper -> container.getChildren().add(
                OperationBlockFactory.createOperationBlock(
                        oper, block -> container.getChildren().remove(block))));
    }

    /* ================= HELPERS ================= */
//
//    private TextField tf(String v) {
//        TextField f = styled(new TextField(), "compact");
//        f.setText(v);
//        return f;
//    }

    private void clearAll(VBox ops, VBox sbor) {
        ops.getChildren().clear();
        sbor.getChildren().clear();
    }
}




