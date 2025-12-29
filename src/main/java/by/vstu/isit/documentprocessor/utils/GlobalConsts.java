package by.vstu.isit.documentprocessor.utils;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalConsts {
    @Getter
    private static String ICON_PATH ;

    @Value("${path.to.icon}")
    private String iconPath;

    @PostConstruct
    private void init() {
        ICON_PATH = iconPath;
    }
}
