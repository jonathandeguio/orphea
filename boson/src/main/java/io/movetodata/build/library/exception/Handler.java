package io.movetodata.build.library.exception;

import io.movetodata.sharedutils.Response.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j(topic = "BUILD_EXCEPTION_HANDLER")
@ControllerAdvice
public class Handler {
    @ExceptionHandler(FunnelServiceException.class)
    public ResponseEntity<Object> handleFunnelServiceException(FunnelServiceException ex) {
        log.info("handleFunnelServiceException", ex);
        ErrorDTO errorResponse = new ErrorDTO(400, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(BuildSpecException.class)
    public ResponseEntity<Object> handleBuildSpecException(BuildSpecException ex) {
        log.info("handleFunnelServiceException", ex);
        ErrorDTO errorResponse = new ErrorDTO(400, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}
