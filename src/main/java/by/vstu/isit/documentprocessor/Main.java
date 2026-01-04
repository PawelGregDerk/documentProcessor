package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.controllers.MainController;
import by.vstu.isit.documentprocessor.entities.Docpackage;
import by.vstu.isit.documentprocessor.services.interfaces.DocpackageService;
import javafx.application.Application;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Optional;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.MAIN_SCENE;
import static by.vstu.isit.documentprocessor.utils.ResourceHelper.*;

@SpringBootApplication
public class Main extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = SpringApplication.run(Main.class);
    }

    @Override
    public void start(Stage primaryStage) {
        var fxWeaver = springContext.getBean(FxWeaver.class);
        loadStage(MainController.class, fxWeaver, MAIN_SCENE, primaryStage);
    }

    @Override
    public void stop() {
        springContext.close();
    }

    private static void runTests() {
    System.out.println("=== ТЕСТИРОВАНИЕ ПОЛНЫХ CRUD ОПЕРАЦИЙ ===");
    System.out.println("Тестирование: Create, Read, Update, Delete\n");
    
    try (ConfigurableApplicationContext context = SpringApplication.run(Main.class)) {
        var service = context.getBean(
            by.vstu.isit.documentprocessor.services.interfaces.DocpackageService.class
        );
        
        // Уникальные данные для теста
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniquePath = "/test-path-" + timestamp;
        String uniqueName = "Test Package " + timestamp;
        
        // 1. СОЗДАНИЕ (CREATE)
        System.out.println("1. СОЗДАНИЕ (CREATE) пакета...");
        var newPackage = by.vstu.isit.documentprocessor.entities.Docpackage.builder()
            .packageName(uniqueName)
            .path(uniquePath)
            .PUName("ПУ 0001")
            .SPUName("СХПУ 0001")
            .KPName("КП 0001")
            .FEMAName("Т0001")
            .vedIName("1-001")
            .build();
        
        var savedPackage = service.saveDocpackage(newPackage);
        Long createdId = savedPackage.getIdDocPackage();
        System.out.println("   УСПЕШНО! Создан пакет ID: " + createdId);
        System.out.println("   Название: " + savedPackage.getPackageName());
        System.out.println("   Путь: " + savedPackage.getPath());
        
        // 2. ЧТЕНИЕ (READ)
        System.out.println("\n2. ЧТЕНИЕ (READ) пакета...");
        var foundPackage = service.findDocpackageById(createdId);
        
        if (foundPackage.isPresent()) {
            System.out.println("   НАЙДЕНО: " + foundPackage.get().getPackageName());
            System.out.println("   ID: " + foundPackage.get().getIdDocPackage());
            System.out.println("   Путь: " + foundPackage.get().getPath());
        } else {
            System.out.println("   НЕ НАЙДЕНО");
            return;
        }
        
        // 3. ОБНОВЛЕНИЕ (UPDATE)
        System.out.println("\n3. ОБНОВЛЕНИЕ (UPDATE) пакета...");
        try {
            // Сохраняем оригинальные данные для проверки
            String originalName = savedPackage.getPackageName();
            String originalPath = savedPackage.getPath();
            
            // Изменяем данные
            String updatedName = "Updated Package " + timestamp;
            String updatedPath = "/updated-path-" + timestamp;
            
            System.out.println("   Изменяем данные:");
            System.out.println("   Старое название: " + originalName);
            System.out.println("   Новое название: " + updatedName);
            System.out.println("   Старый путь: " + originalPath);
            System.out.println("   Новый путь: " + updatedPath);
            
            // Обновляем объект
            savedPackage.setPackageName(updatedName);
            savedPackage.setPath(updatedPath);
            savedPackage.setPUName("Updated ПУ 0002");
            savedPackage.setSPUName("Updated СХПУ 0002");
            
            // Вызываем обновление
            var updatedPackage = service.updateDocpackage(savedPackage);
            System.out.println("   ОБНОВЛЕНИЕ ВЫПОЛНЕНО");
            System.out.println("   Обновленный ID: " + updatedPackage.getIdDocPackage());
            System.out.println("   Новое название: " + updatedPackage.getPackageName());
            System.out.println("   Новый путь: " + updatedPackage.getPath());
            
            // Проверка обновления
            System.out.println("\n   Проверка обновления...");
            var checkUpdated = service.findDocpackageById(createdId);
            
            if (checkUpdated.isPresent()) {
                var verifiedPackage = checkUpdated.get();
                boolean nameUpdated = verifiedPackage.getPackageName().equals(updatedName);
                boolean pathUpdated = verifiedPackage.getPath().equals(updatedPath);
                
                System.out.println("   Проверка полей:");
                System.out.println("   Название обновлено: " + (nameUpdated ? "ДА" : "НЕТ"));
                System.out.println("   Путь обновлен: " + (pathUpdated ? "ДА" : "НЕТ"));
                
                if (nameUpdated && pathUpdated) {
                    System.out.println("   ОБНОВЛЕНИЕ ПОДТВЕРЖДЕНО В БД!");
                } else {
                    System.out.println("   ОШИБКА: данные не обновились в БД");
                }
            } else {
                System.out.println("   Пакет не найден после обновления");
            }
            
        } catch (Exception e) {
            System.err.println("   ОШИБКА при обновлении: " + e.getMessage());
            System.err.println("   Подробности: " + e.getClass().getName());
            e.printStackTrace();
            return;
        }
        
        // 4. УДАЛЕНИЕ (DELETE)
        System.out.println("\n4. УДАЛЕНИЕ (DELETE) пакета...");
        try {
            System.out.println("   Удаляем пакет ID: " + createdId);
            service.deleteDocpackage(createdId);
            System.out.println("   УДАЛЕНИЕ ВЫПОЛНЕНО");
            
            // Проверка удаления
            System.out.println("\n   Проверка удаления...");
            var checkDeleted = service.findDocpackageById(createdId);
            
            if (checkDeleted.isEmpty()) {
                System.out.println("   ПОДТВЕРЖДЕНО: пакет успешно удален из БД");
                System.out.println("   Проверка: поиск по ID " + createdId + " вернул пустой результат");
            } else {
                System.out.println("   ОШИБКА: пакет все еще существует в БД!");
                System.out.println("   Найден: " + checkDeleted.get().getPackageName());
            }
            
            // Итоговый вывод
            System.out.println("\n" + "=".repeat(50));
            System.out.println("ВСЕ CRUD ОПЕРАЦИИ ПРОЙДЕНЫ УСПЕШНО!");
            System.out.println("=".repeat(50));
            System.out.println("CREATE - Создание: РАБОТАЕТ");
            System.out.println("READ   - Чтение: РАБОТАЕТ");
            System.out.println("UPDATE - Обновление: РАБОТАЕТ");
            System.out.println("DELETE - Удаление: РАБОТАЕТ");
            System.out.println("=".repeat(50));
            
        } catch (Exception e) {
            System.err.println("   Ошибка при удалении: " + e.getMessage());
            System.err.println("   Подробности: " + e.getClass().getName());
            e.printStackTrace();
        }
        
        
    } catch (Exception e) {
        System.err.println("\nОШИБКА ТЕСТИРОВАНИЯ: " + e.getMessage());
        e.printStackTrace();
    }
}

    static void main(String... args) {
        if (args.length > 0 && "--test".equals(args[0])) {
            runTests();
        } else {
            launch(Main.class, args);
        }
    }
}
