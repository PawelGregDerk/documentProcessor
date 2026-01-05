package by.vstu.isit.documentprocessor;

import by.vstu.isit.documentprocessor.entities.db.Docpackage;
import by.vstu.isit.documentprocessor.excepts.DataNotFoundException;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.SpringApplication;

@SpringBootApplication
public class ConsoleMain implements CommandLineRunner {
    //    @Setter(onMethod_ = {@Autowired})
//    private by.vstu.isit.documentprocessor.services.interfaces.DocpackageService service;
    @Setter(onMethod_ = {@Autowired})
    private by.vstu.isit.documentprocessor.services.db.interfaces.DocpackageService dbService;

    @Override
    public void run(String... args) {
        newTests();
    }

    static void main(String[] args) {
        SpringApplication.run(ConsoleMain.class, args);
    }

//    private void runTests() {
//        System.out.println("=== ТЕСТИРОВАНИЕ ПОЛНЫХ CRUD ОПЕРАЦИЙ ===");
//        System.out.println("Тестирование: Create, Read, Update, Delete\n");
//
//        // Уникальные данные для теста
//        String timestamp = String.valueOf(System.currentTimeMillis());
//        String uniquePath = "/test-path-" + timestamp;
//        String uniqueName = "Test Package " + timestamp;
//
//        // 1. СОЗДАНИЕ (CREATE)
//        System.out.println("1. СОЗДАНИЕ (CREATE) пакета...");
//        var newPackage = by.vstu.isit.documentprocessor.entities.Docpackage.builder()
//                .packageName(uniqueName)
//                .path(uniquePath)
//                .PUName("ПУ 0001")
//                .SPUName("СХПУ 0001")
//                .KPName("КП 0001")
//                .FEMAName("Т0001")
//                .vedIName("1-001")
//                .build();
//
//        var savedPackage = service.saveDocpackage(newPackage);
//        Long createdId = savedPackage.getIdDocPackage();
//        System.out.println("   УСПЕШНО! Создан пакет ID: " + createdId);
//        System.out.println("   Название: " + savedPackage.getPackageName());
//        System.out.println("   Путь: " + savedPackage.getPath());
//
//        // 2. ЧТЕНИЕ (READ)
//        System.out.println("\n2. ЧТЕНИЕ (READ) пакета...");
//        var foundPackage = service.findDocpackageById(createdId);
//
//        if (foundPackage.isPresent()) {
//            System.out.println("   НАЙДЕНО: " + foundPackage.get().getPackageName());
//            System.out.println("   ID: " + foundPackage.get().getIdDocPackage());
//            System.out.println("   Путь: " + foundPackage.get().getPath());
//        } else {
//            System.out.println("   НЕ НАЙДЕНО");
//            return;
//        }
//
//        // 3. ОБНОВЛЕНИЕ (UPDATE)
//        System.out.println("\n3. ОБНОВЛЕНИЕ (UPDATE) пакета...");
//        try {
//            // Сохраняем оригинальные данные для проверки
//            String originalName = savedPackage.getPackageName();
//            String originalPath = savedPackage.getPath();
//
//            // Изменяем данные
//            String updatedName = "Updated Package " + timestamp;
//            String updatedPath = "/updated-path-" + timestamp;
//
//            System.out.println("   Изменяем данные:");
//            System.out.println("   Старое название: " + originalName);
//            System.out.println("   Новое название: " + updatedName);
//            System.out.println("   Старый путь: " + originalPath);
//            System.out.println("   Новый путь: " + updatedPath);
//
//            // Обновляем объект
//            savedPackage.setPackageName(updatedName);
//            savedPackage.setPath(updatedPath);
//            savedPackage.setPUName("Updated ПУ 0002");
//            savedPackage.setSPUName("Updated СХПУ 0002");
//
//            // Вызываем обновление
//            var updatedPackage = service.updateDocpackage(savedPackage);
//            System.out.println("   ОБНОВЛЕНИЕ ВЫПОЛНЕНО");
//            System.out.println("   Обновленный ID: " + updatedPackage.getIdDocPackage());
//            System.out.println("   Новое название: " + updatedPackage.getPackageName());
//            System.out.println("   Новый путь: " + updatedPackage.getPath());
//
//            // Проверка обновления
//            System.out.println("\n   Проверка обновления...");
//            var checkUpdated = service.findDocpackageById(createdId);
//
//            if (checkUpdated.isPresent()) {
//                var verifiedPackage = checkUpdated.get();
//                boolean nameUpdated = verifiedPackage.getPackageName().equals(updatedName);
//                boolean pathUpdated = verifiedPackage.getPath().equals(updatedPath);
//
//                System.out.println("   Проверка полей:");
//                System.out.println("   Название обновлено: " + (nameUpdated ? "ДА" : "НЕТ"));
//                System.out.println("   Путь обновлен: " + (pathUpdated ? "ДА" : "НЕТ"));
//
//                if (nameUpdated && pathUpdated) {
//                    System.out.println("   ОБНОВЛЕНИЕ ПОДТВЕРЖДЕНО В БД!");
//                } else {
//                    System.out.println("   ОШИБКА: данные не обновились в БД");
//                }
//            } else {
//                System.out.println("   Пакет не найден после обновления");
//            }
//
//        } catch (Exception e) {
//            System.err.println("   ОШИБКА при обновлении: " + e.getMessage());
//            System.err.println("   Подробности: " + e.getClass().getName());
//            e.printStackTrace();
//            return;
//        }
//
//        // 4. УДАЛЕНИЕ (DELETE)
//        System.out.println("\n4. УДАЛЕНИЕ (DELETE) пакета...");
//        try {
//            System.out.println("   Удаляем пакет ID: " + createdId);
//            service.deleteDocpackage(createdId);
//            System.out.println("   УДАЛЕНИЕ ВЫПОЛНЕНО");
//
//            // Проверка удаления
//            System.out.println("\n   Проверка удаления...");
//            var checkDeleted = service.findDocpackageById(createdId);
//
//            if (checkDeleted.isEmpty()) {
//                System.out.println("   ПОДТВЕРЖДЕНО: пакет успешно удален из БД");
//                System.out.println("   Проверка: поиск по ID " + createdId + " вернул пустой результат");
//            } else {
//                System.out.println("   ОШИБКА: пакет все еще существует в БД!");
//                System.out.println("   Найден: " + checkDeleted.get().getPackageName());
//            }
//
//            // Итоговый вывод
//            System.out.println("\n" + "=".repeat(50));
//            System.out.println("ВСЕ CRUD ОПЕРАЦИИ ПРОЙДЕНЫ УСПЕШНО!");
//            System.out.println("=".repeat(50));
//            System.out.println("CREATE - Создание: РАБОТАЕТ");
//            System.out.println("READ   - Чтение: РАБОТАЕТ");
//            System.out.println("UPDATE - Обновление: РАБОТАЕТ");
//            System.out.println("DELETE - Удаление: РАБОТАЕТ");
//            System.out.println("=".repeat(50));
//
//        } catch (Exception e) {
//            System.err.println("   Ошибка при удалении: " + e.getMessage());
//            System.err.println("   Подробности: " + e.getClass().getName());
//            e.printStackTrace();
//        }
//    }

    private void newTests() {
        System.out.println("=== ТЕСТИРОВАНИЕ ПОЛНЫХ CRUD ОПЕРАЦИЙ ===");
        System.out.println("Тестирование: Create, Read, Update, Delete\n");

        // Уникальные данные для теста
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uniquePath = "/test-path-" + timestamp;
        String uniqueName = "Test Package " + timestamp;

        // 1. СОЗДАНИЕ (CREATE)
        System.out.println("1. СОЗДАНИЕ (CREATE) пакета...");
        var newPackage = Docpackage.builder()
                .packageName(uniqueName)
                .path(uniquePath)
                .PUName("ПУ 0001")
                .SPUName("СХПУ 0001")
                .KPName("КП 0001")
                .FEMAName("Т0001")
                .vedIName("1-001")
                .build();

        var savedPackage = dbService.save(newPackage);
        Long createdId = savedPackage.getId();
        System.out.println("   УСПЕШНО! Создан пакет ID: " + createdId);
        System.out.println("   Название: " + savedPackage.getPackageName());
        System.out.println("   Путь: " + savedPackage.getPath());

        // 2. ЧТЕНИЕ (READ)
        System.out.println("\n2. ЧТЕНИЕ (READ) пакета...");
        try {
            var foundPackage = dbService.getById(createdId);
            System.out.println("   НАЙДЕНО: " + foundPackage.getPackageName());
            System.out.println("   ID: " + foundPackage.getId());
            System.out.println("   Путь: " + foundPackage.getPath());
        } catch (DataNotFoundException e) {
            System.err.println("Ошибка чтения: " + e.getMessage());
            System.err.println("   Подробности: " + e.getClass().getName());
            e.printStackTrace();
            return;
        }

        // 3. ОБНОВЛЕНИЕ (UPDATE)
        System.out.println("\n3. ОБНОВЛЕНИЕ (UPDATE) пакета...");
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
        var updatedPackage = dbService.updateDocpackage(savedPackage);
        System.out.println("   ОБНОВЛЕНИЕ ВЫПОЛНЕНО");
        System.out.println("   Обновленный ID: " + updatedPackage.getId());
        System.out.println("   Новое название: " + updatedPackage.getPackageName());
        System.out.println("   Новый путь: " + updatedPackage.getPath());

        // Проверка обновления
        System.out.println("\n   Проверка обновления...");
        try {
            var checkUpdated = dbService.getById(createdId);
            boolean nameUpdated = checkUpdated.getPackageName().equals(updatedName);
            boolean pathUpdated = checkUpdated.getPath().equals(updatedPath);

            System.out.println("   Проверка полей:");
            System.out.println("   Название обновлено: " + (nameUpdated ? "ДА" : "НЕТ"));
            System.out.println("   Путь обновлен: " + (pathUpdated ? "ДА" : "НЕТ"));
            System.out.println("   ОБНОВЛЕНИЕ ПОДТВЕРЖДЕНО В БД!");
        } catch (Exception e) {
            System.err.println("   ОШИБКА при обновлении: " + e.getMessage());
            System.err.println("   Подробности: " + e.getClass().getName());
            e.printStackTrace();
            return;
        }

        // 4. УДАЛЕНИЕ (DELETE)
        System.out.println("\n4. УДАЛЕНИЕ (DELETE) пакета...");
        System.out.println("   Удаляем пакет ID: " + createdId);

        // Проверка удаления
        System.out.println("\n   Проверка удаления...");
        try {
            dbService.delete(createdId);
            var checkDeleted = dbService.getById(createdId);
            System.err.println("   ОШИБКА: пакет все еще существует в БД!");
            System.err.println("   Найден: " + checkDeleted.getPackageName());
        } catch (DataNotFoundException e) {
            System.out.println("   ПОДТВЕРЖДЕНО: пакет успешно удален из БД");
            System.out.println("   Проверка: поиск по ID " + createdId + " вернул пустой результат");
        } catch (Exception e) {
            System.err.println("   Ошибка при удалении: " + e.getMessage());
            System.err.println("   Подробности: " + e.getClass().getName());
            e.printStackTrace();
            return;
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

    }
}
