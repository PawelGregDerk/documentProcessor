package by.vstu.isit.documentprocessor.utils;

import lombok.experimental.UtilityClass;

import java.util.Locale;
import java.util.ResourceBundle;

import static java.text.MessageFormat.format;

@UtilityClass
public class LocalizeHelper {
    private final Locale CURRENT_LOCALE = Locale.getDefault();

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle("i18n.messages", CURRENT_LOCALE);
    }

    @SafeVarargs
    public <T> String getMessage(MessageCodes code, T... args) {
        return args.length == 0
                ? getBundle().getString(code.getCode())
                : format(getBundle().getString(code.getCode()), args);
    }
}
