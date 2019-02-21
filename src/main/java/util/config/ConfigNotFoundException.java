package util.config;

public class ConfigNotFoundException extends Exception {

    private static final long serialVersionUID = 1L;

    public ConfigNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigNotFoundException(String message) {
        super(message);
    }
}
