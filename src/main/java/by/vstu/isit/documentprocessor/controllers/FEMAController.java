package by.vstu.isit.documentprocessor.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@FxmlView("create-fema-view.fxml")
@RequiredArgsConstructor
public class FEMAController {

    public void onSaveButtonClick() {
//        loadStage(OperationToDocxController.class, fxWeaver, mainVBox, SELECT_OP_TITLE);
    }

}
