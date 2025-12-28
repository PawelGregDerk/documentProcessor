package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.repositories.TypeOperRepository;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.*;
import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@Slf4j
@Controller
@FxmlView("main-view.fxml")
@RequiredArgsConstructor
public class MainController {
    private final FxWeaver fxWeaver;

    @FXML
    private VBox mainVBox;

    public void createDocPackage() {
        loadStage(DocPackageController.class, fxWeaver, mainVBox, NEW_DOC_PACKAGE);
   }
}
