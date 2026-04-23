package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.dto.FuncDto;
import by.vstu.isit.documentprocessor.dto.OperDto;
import by.vstu.isit.documentprocessor.dto.SborEdDto;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageFormMapper;
import by.vstu.isit.documentprocessor.mappers.gui.DockPackageGuiMapper;
import by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService;
import by.vstu.isit.documentprocessor.services.docx.write.abstracts.AbstractWordGenerator;
import io.vavr.control.Try;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import java.util.List;

import static by.vstu.isit.documentprocessor.utils.GuiHelper.*;

@Slf4j
@Controller
@FxmlView("docpackage-test-view.fxml")
@RequiredArgsConstructor
public class DocPackageTestCreateController {
    @FXML
    private TextField packageNameField;
    @FXML
    private TextField puField;
    @FXML
    private TextField spuField;
    @FXML
    private TextField kpField;
    @FXML
    private TextField fmeaField;
    @FXML
    private TextField vedInstrField;
    @FXML
    private TextField sborEdNazv;
    @FXML
    private VBox assemblyUnitsContainer;
    @FXML
    private VBox operationsContainer;
    @FXML
    private TextField packagePathField;

    private final DockPackageFormMapper formMapper;
    private final DockPackageGuiMapper guiMapper;
    private final DocpackageService service;
    private final List<AbstractWordGenerator> generators;

    @FXML
    private void initialize() {
        DockPackageDto dto = buildTestDto();
        guiMapper.toGui(
                dto,
                packageNameField,
                sborEdNazv,
                puField,
                spuField,
                kpField,
                fmeaField,
                vedInstrField,
                operationsContainer,
                assemblyUnitsContainer
        );
        packagePathField.setText(dto.path());
    }

    @FXML
    private void onAddAssemblyUnit() {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);

        TextField codeField = styled(new TextField(), "compact");
        codeField.setPromptText("РћР±РѕР·РЅР°С‡РµРЅРёРµ СЃР±РѕСЂРѕС‡РЅРѕР№ РµРґРёРЅРёС†С‹");
        codeField.prefWidthProperty().bind(row.widthProperty().multiply(0.75));

        Button deleteButton = styled(new Button("РЈРґР°Р»РёС‚СЊ"), "delButton");
        deleteButton.setOnAction(e -> assemblyUnitsContainer.getChildren().remove(row));

        row.getChildren().addAll(codeField, deleteButton);
        assemblyUnitsContainer.getChildren().add(row);
    }

    @FXML
    private void onAddOperation() {
        VBox operationBlock = styled(new VBox(5), "operation-block");
        HBox operRow = styled(new HBox(6), "operation-row");
        operRow.setAlignment(Pos.CENTER_LEFT);
        TextField numOper = styled(new TextField(), "compact");
        numOper.setPromptText("в„– РѕРїРµСЂР°С†РёРё");
        TextField nomInstr = styled(new TextField(), "compact");
        nomInstr.setPromptText("в„– РёРЅСЃС‚СЂСѓРєС†РёРё");
        TextField oborud = new TextField();
        oborud.setPromptText("РћР±РѕСЂСѓРґРѕРІР°РЅРёРµ");
        TextField ostnas = new TextField();
        ostnas.setPromptText("РћСЃРЅР°СЃС‚РєР° / РРЅСЃС‚СЂСѓРјРµРЅС‚");
        TextField name = new TextField();
        name.setPromptText("РќР°РёРјРµРЅРѕРІР°РЅРёРµ");
        TextField shifr = styled(new TextField(), "compact");
        shifr.setPromptText("РЁРёС„СЂ");
        TextField zech = styled(new TextField(), "compact");
        zech.setPromptText("Р¦РµС…");

        Button addFuncBtn = styled(new Button("Р”РѕР±Р°РІРёС‚СЊ С„СѓРЅРєС†РёСЋ"), "btn-primary", "btn-small");
        addFuncBtn.setPrefWidth(140);
        Button delOperBtn = styled(new Button("РЈРґР°Р»РёС‚СЊ"), "btn-danger", "btn-small");
        delOperBtn.setPrefWidth(90);

        operRow.getChildren().addAll(
                numOper, nomInstr, oborud, ostnas, name, shifr, zech, addFuncBtn, delOperBtn
        );

        VBox funcsContainer = styled(new VBox(4), "funcs-container");

        addFuncBtn.setOnAction(e -> addFunctionRow(funcsContainer));
        delOperBtn.setOnAction(e -> operationsContainer.getChildren().remove(operationBlock));
        operationBlock.getChildren().addAll(operRow, funcsContainer);
        operationsContainer.getChildren().add(operationBlock);
    }

    private void addFunctionRow(VBox funcsContainer) {
        if (funcsContainer.getChildren().isEmpty()) {
            HBox header = styled(new HBox(6), "function-header");
            header.getStyleClass().add("function-header-row");
            header.getChildren().addAll(
                    styled(new Label("РћРїРёСЃР°РЅРёРµ С„СѓРЅРєС†РёРё"), "table-header"),
                    styled(new Label("РџР°СЂР°РјРµС‚СЂС‹ / РўСЂРµР±РѕРІР°РЅРёСЏ"), "table-header"),
                    styled(new Label("РџСЂРѕРґСѓРєС‚"), "table-header"),
                    styled(new Label("РЎРїРµС†. С…Р°СЂР°РєС‚РµСЂРёСЃС‚РёРєРё"), "table-header")
            );
            funcsContainer.getChildren().add(header);
        }

        HBox funcRow = styled(new HBox(6), "function-row");
        funcRow.getStyleClass().add("function-data-row");
        funcRow.setAlignment(Pos.CENTER_LEFT);

        TextField name = styled(new TextField(), "compact");
        name.setPromptText("РћРїРёСЃР°РЅРёРµ");
        TextField param = styled(new TextField(), "no-compact");
        param.setPromptText("РџР°СЂР°РјРµС‚СЂС‹");
        CheckBox isProd = new CheckBox();
        TextField spec = styled(new TextField(), "compact");
        spec.setPromptText("РЎРїРµС†. С…Р°СЂР°РєС‚РµСЂРёСЃС‚РёРєРё");

        Button delBtn = styled(new Button("РЈРґР°Р»РёС‚СЊ"), "delButton");
        delBtn.setOnAction(e -> {
            funcsContainer.getChildren().remove(funcRow);
            boolean hasDataRows = funcsContainer.getChildren().stream()
                    .anyMatch(n -> n.getStyleClass().contains("function-data-row"));
            if (!hasDataRows) {
                funcsContainer.getChildren().removeIf(
                        n -> n.getStyleClass().contains("function-header-row")
                );
            }
        });

        funcRow.getChildren().addAll(name, param, isProd, spec, delBtn);
        funcsContainer.getChildren().add(funcRow);
    }

    @FXML
    private void onSave() {
        try {
            var pckgDto = formMapper.fromGui(
                    null,
                    packageNameField,
                    packagePathField.getText(),
                    sborEdNazv,
                    puField,
                    spuField,
                    kpField,
                    fmeaField,
                    vedInstrField,
                    operationsContainer,
                    assemblyUnitsContainer
            );
            var dto = service.saveFullPackage(pckgDto);
            generators.forEach(g -> Try.run(() -> g.generate(dto))
                    .onFailure(ex -> log.error("РћС€РёР±РєР° РіРµРЅРµСЂР°С†РёРё РґРѕРєСѓРјРµРЅС‚Р°", ex))
                    .getOrElseThrow(ex -> new RuntimeException(ex)));
            new Alert(Alert.AlertType.INFORMATION, "Р”РѕРєСѓРјРµРЅС‚ СЃС„РѕСЂРјРёСЂРѕРІР°РЅ").showAndWait();
        } catch (Exception ex) {
            log.error("РћС€РёР±РєР° РіРµРЅРµСЂР°С†РёРё", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        }
    }

    private DockPackageDto buildTestDto() {
        return new DockPackageDto(
                null,
                "пакет_1",
                "111.115",
                "пу_1",
                "схпу_1",
                "кп_1",
                "фмеа_1",
                "ви_1",
                List.of(
                        op(".01", "№_1", "об_1", "ои_1", "оп_1", "ш_1", "1_1", List.of(
                                func("оп_1_ф_1", "оп_1_фп_1", "оп_1_сх_1"),
                                func("оп_1_ф_2", "оп_1_фп_2", "оп_1_сх_2")
                        )),
                        op(".02", "№_2", "об_2", "ои_2", "оп_2", "ш_2", "2_2", List.of(
                                func("оп_2_ф_1", "оп_2_фп_1", "оп_2_сх_1"),
                                func("оп_2_ф_2", "оп_2_фп_2", "оп_2_сх_2")
                        )),
                        op(".03", "№_3", "об_3", "ои_3", "оп_3", "ш_3", "3_3", List.of(
                                func("оп_3_ф_1", "оп_3_фп_1", "оп_3_сх_1"),
                                func("оп_3_ф_2", "оп_3_фп_2", "оп_3_сх_2"),
                                func("оп_3_ф_3", "оп_3_фп_3", "оп_3_сх_3")
                        )),
                        op(".04", "№_4", "об_4", "ои_4", "оп_4", "ш_4", "4_4", List.of(
                                func("оп_4_ф_1", "оп_4_фп_1", "оп_4_сх_1"),
                                func("оп_4_ф_2", "оп_4_фп_2", "оп_4_сх_2"),
                                func("оп_4_ф_3", "оп_4_фп_3", "оп_4_сх_3")
                        )),
                        op(".05", "№_5", "об_5", "ои_5", "оп_5", "ш_5", "5_5", List.of(
                                func("оп_5_ф_1", "оп_5_фп_1", "оп_5_сх_1"),
                                func("оп_5_ф_2", "оп_5_фп_2", "оп_5_сх_2"),
                                func("оп_5_ф_3", "оп_5_фп_3", "оп_5_сх_3")
                        ))
                ),
                List.of(
                        new SborEdDto(null, null, "СборЕд_1", "се_1-001"),
                        new SborEdDto(null, null, "СборЕд_1", "се_2-002"),
                        new SborEdDto(null, null, "СборЕд_1", "се_3-003")
                )
        );
    }

    private OperDto op(
            String numOper,
            String nomInstr,
            String oborud,
            String ostnasInstr,
            String name,
            String shifr,
            String numZech,
            List<FuncDto> funcs
    ) {
        return new OperDto(
                null,
                null,
                numOper,
                nomInstr,
                null,
                oborud,
                ostnasInstr,
                name,
                shifr,
                numZech,
                funcs
        );
    }

    private FuncDto func(String name, String param, String spec) {
        return new FuncDto(null, name, null, param, false, spec);
    }
}
