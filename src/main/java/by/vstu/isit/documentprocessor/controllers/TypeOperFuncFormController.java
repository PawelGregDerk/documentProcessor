package by.vstu.isit.documentprocessor.controllers;

import by.vstu.isit.documentprocessor.entities.TypeOperFunc;
//import jakarta.persistence.EntityManager;
//import jakarta.persistence.EntityManagerFactory;
//import jakarta.persistence.Persistence;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@FxmlView("typeOperFunc.fxml")
@RequiredArgsConstructor
public class TypeOperFuncFormController {
    @FXML
    private TextField nameField;

    @FXML
    private TextField paramField;

    @FXML
    private CheckBox isProdCheckBox;

    @FXML
    private TextField specCharaktField;

    @FXML
    private Button saveButton;

//    private EntityManagerFactory emf;
//    private EntityManager em;

    public void initialize() {
        // Создаём EntityManager
//        emf = Persistence.createEntityManagerFactory("documentProcessorPU"); // имя persistence-unit
//        em = emf.createEntityManager();

       // saveButton.setOnAction(event -> saveTypeOperFunc());
    }

    private void saveTypeOperFunc() {
        String name = nameField.getText().trim();
        String param = paramField.getText().trim();
        boolean isProd = isProdCheckBox.isSelected();
        String specCharakt = specCharaktField.getText().trim();

        if (name.isEmpty() || param.isEmpty()) {
            // Можно добавить Alert для предупреждения
            System.out.println("Name и Param обязательны для заполнения");
            return;
        }

        TypeOperFunc typeOperFunc = TypeOperFunc.builder()
                .idTypeFunc(System.currentTimeMillis()) // временный уникальный id
                .name(name)
                .param(param)
                .isProd(isProd)
                .specCharakt(specCharakt.isEmpty() ? null : specCharakt)
                .idTypeOper(1L) // пример: связанная операция (позже можно добавить выбор)
                .build();

//        try {
//            em.getTransaction().begin();
//            em.persist(typeOperFunc);
//            em.getTransaction().commit();
//            System.out.println("TypeOperFunc сохранён");
//            closeForm();
//        } catch (Exception e) {
//            em.getTransaction().rollback();
//            e.printStackTrace();
//        }
    }

    private void closeForm() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

//    public void dispose() {
//        if (em != null) em.close();
//        if (emf != null) emf.close();
//    }
}
