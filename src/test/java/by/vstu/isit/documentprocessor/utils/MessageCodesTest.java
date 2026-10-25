package by.vstu.isit.documentprocessor.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MessageCodesTest {

    @Test
    @DisplayName("getCode: all MessageCodes enum constants have non-blank codes")
    void getCode_allConstants_haveValidCodes() {
        for (MessageCodes mc : MessageCodes.values()) {
            assertNotNull(mc.getCode(), "Code for " + mc.name() + " should not be null");
            assertFalse(mc.getCode().isBlank(), "Code for " + mc.name() + " should not be blank");
        }
    }

    @Test
    @DisplayName("valueOf: resolves expected enum constant")
    void valueOf_validName_returnsEnumConstant() {
        assertEquals(MessageCodes.CREATE_SUCCESS, MessageCodes.valueOf("CREATE_SUCCESS"));
        assertEquals("create.success", MessageCodes.CREATE_SUCCESS.getCode());
        assertEquals("wrong.operation", MessageCodes.WRONG_OPERATION.getCode());
        assertEquals("data.not.found", MessageCodes.DATA_NOT_FOUND.getCode());
    }
}
