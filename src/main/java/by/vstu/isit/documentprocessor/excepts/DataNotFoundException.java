package by.vstu.isit.documentprocessor.excepts;

public class DataNotFoundException extends RuntimeException {
    public DataNotFoundException(String msg) {
        super(msg);
    }

    public DataNotFoundException() {
        this("data.not.found");
    }
}
