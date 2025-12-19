package by.vstu.isit.documentprocessor.utils;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum MessageCodes {
    CREATE_SUCCESS("create.success"),
    WRONG_OPERATION("wrong.operation"),
    SAVE_OP_SUCCESS("save.op.success"),
    SAVE_OP_ERROR("save.op.error"),
    SAVE_OP_TITLE("save.op.title"),
    SELECT_OP_TITLE("select.op.title"),
    SELECT_OP_ACTION("select.op.action"),
    CREATE_OP_ERROR("create.op.error"),
    MAIN_SCENE("main.scene"),
    FILE_SAVING("file.saving"),
    SAVE_TITLE("save.title"),
    SAVE_PATH("save.path"),
    SELECT_DOC_TITLE("select.doc"),
    EDIT_DOC("edit.doc"),;

    @Getter
    private final String code;
}
