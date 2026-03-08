package by.vstu.isit.documentprocessor.utils;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;

public final class LocalizeHelper {
    private static final Locale CURRENT_LOCALE = Locale.getDefault();

    private LocalizeHelper() {
    }

    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle("i18n.messages", CURRENT_LOCALE);
    }

    public static String getMessage(MessageCodes code, Object... args) {
        return args.length == 0
                ? getBundle().getString(code.getCode())
                : format(getBundle().getString(code.getCode()), args);
    }
}
