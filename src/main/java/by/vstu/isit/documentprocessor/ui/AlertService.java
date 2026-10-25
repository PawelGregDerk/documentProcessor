package by.vstu.isit.documentprocessor.ui;

public interface AlertService {
    void showError(String message);
    void showInfo(String message);
    boolean confirm(String title, String header, String content);
}
