package by.vstu.isit.documentprocessor.test;

import by.vstu.isit.documentprocessor.dto.DockPackageDto;
import by.vstu.isit.documentprocessor.services.docx.DocxDocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Тестовый класс для проверки функционала переименования документов
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DocumentRenameTest {
    private final List<DocxDocumentService> services;
    private final TestDockPackageFactory factory;

    public void testDocumentRename() {
        try {
            // Создаем оригинальный DTO
            DockPackageDto originalDto = factory.createTestDto();
            
            // Создаем измененный DTO с новыми названиями документов
            DockPackageDto modifiedDto = new DockPackageDto(
                    originalDto.id(),
                    originalDto.packageName(),
                    originalDto.path(),
                    "ПУ 0099 НОВЫЙ", // изменено
                    originalDto.spuName(),
                    "КП0059 НОВЫЙ", // изменено
                    "FMEA T 0115 НОВЫЙ", // изменено
                    "15-133 НОВЫЙ", // изменено
                    originalDto.opers(),
                    originalDto.sborEds()
            );

            log.info("Тестирование переименования документов...");
            
            // Сначала генерируем оригинальные документы
            services.forEach(service -> {
                try {
                    service.upsert(originalDto);
                    log.info("Сгенерирован оригинальный документ для сервиса: {}", service.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Ошибка генерации оригинального документа для {}: {}", service.getClass().getSimpleName(), e.getMessage());
                }
            });

            // Затем обновляем с переименованием
            services.forEach(service -> {
                try {
                    service.upsert(modifiedDto, originalDto);
                    log.info("Обновлен документ с переименованием для сервиса: {}", service.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Ошибка обновления с переименованием для {}: {}", service.getClass().getSimpleName(), e.getMessage());
                }
            });

            log.info("Тестирование завершено. Проверьте папку 'копия{}' для результатов.", originalDto.path());
            
        } catch (Exception e) {
            log.error("Ошибка тестирования переименования документов", e);
        }
    }
}