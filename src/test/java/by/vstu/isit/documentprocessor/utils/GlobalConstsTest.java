package by.vstu.isit.documentprocessor.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalConstsTest {

    @Test
    @DisplayName("init: sets static ICON_PATH from injected iconPath")
    void init_setsStaticIconPath() {
        GlobalConsts consts = new GlobalConsts();
        ReflectionTestUtils.setField(consts, "iconPath", "/icons/app-icon.png");

        ReflectionTestUtils.invokeMethod(consts, "init");

        assertEquals("/icons/app-icon.png", GlobalConsts.getICON_PATH());
    }
}
