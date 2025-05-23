package dto.response;

public class ApiResponse<T> {

    private String message;
    private T data;
    private boolean success;

    // Getters
    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public boolean isSuccess() {
        return success;
    }

    // Setters
    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Static methods
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage(message);
        response.setData(data);
        response.setSuccess(true);
        return response;
    }


    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}