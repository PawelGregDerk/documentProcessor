package by.vstu.isit.documentprocessor.excepts;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.DATA_NOT_FOUND;
import by.vstu.isit.documentprocessor.utils.LocalizeHelper;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String msg) {
        super(msg);
    }

    public DataNotFoundException() {
        this(LocalizeHelper.getMessage(DATA_NOT_FOUND));
    }
}
