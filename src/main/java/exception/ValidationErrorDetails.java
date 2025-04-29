package exception;

import java.util.Date;
import java.util.Map;

public class ValidationErrorDetails extends ErrorDetails {
    private Map<String, String> errors;

    public ValidationErrorDetails(Date timestamp, String message, String details, Map<String, String> errors) {
        super(timestamp, message, details);
        this.errors = errors;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}
