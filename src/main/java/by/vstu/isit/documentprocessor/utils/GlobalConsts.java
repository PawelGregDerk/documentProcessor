package by.vstu.isit.documentprocessor.utils;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class GlobalConsts {
    private static String ICON_PATH;

    @Value("${path.to.icon}")
    private String iconPath;

    @PostConstruct
    private void init() {
        ICON_PATH = iconPath;
    }

    public static String getICON_PATH() {
        return ICON_PATH;
    }
}
