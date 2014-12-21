package us.wmwm.happyschedule.api;

/**
 * Created by gravener on 12/20/14.
 */
public class ApiException extends RuntimeException {

    int code;

    public ApiException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
