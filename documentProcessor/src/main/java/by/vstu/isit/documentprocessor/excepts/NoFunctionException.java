package by.vstu.isit.documentprocessor.excepts;

import by.vstu.isit.documentprocessor.utils.LocalizeHelper;

import static by.vstu.isit.documentprocessor.utils.MessageCodes.NO_FUNC;

public class NoFunctionException extends RuntimeException {
    public NoFunctionException(String msg) {
        super(msg);
    }

    public NoFunctionException(String numOp, String nameOp) {
        this(LocalizeHelper.getMessage(NO_FUNC, numOp, nameOp));
    }
}
