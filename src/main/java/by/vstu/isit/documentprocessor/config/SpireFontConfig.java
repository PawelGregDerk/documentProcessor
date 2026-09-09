package by.vstu.isit.documentprocessor.config;

import com.spire.doc.Document;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Slf4j
@Component
public class SpireFontConfig {

    @PostConstruct
    public void registerFonts() {
        try (InputStream arialNarrow = getClass().getResourceAsStream("/fonts/arialnarrow.ttf")) {
            if (arialNarrow != null) {
                Document.setGlobalCustomFonts(new InputStream[]{arialNarrow});
                log.info("Registered custom font: Arial Narrow");
            } else {
                log.warn("arialnarrow.ttf not found in resources");
            }
        } catch (Exception e) {
            log.error("Failed to register custom fonts: {}", e.getMessage());
        }
    }
}
