package pths.server.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class HttpExceptionControllerAdvice {
    @ExceptionHandler(HttpException.class)
    public ResponseEntity<ErrorResponse> handleException(HttpException e)  {
        return new ResponseEntity<>(new ErrorResponse(e.getMessage()), e.getStatus());
    }
}
