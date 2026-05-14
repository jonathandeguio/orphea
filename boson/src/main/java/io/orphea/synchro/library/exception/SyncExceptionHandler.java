package io.orphea.synchro.library.exception;

import io.orphea.sharedutils.Response.ErrorDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j(topic = "SYNC_EXCEPTION_HANDLER")
@ControllerAdvice
public class SyncExceptionHandler {
    @ExceptionHandler(SyncException.class)
    public ResponseEntity<Object> handleSyncException(SyncException ex) {
        log.info("handleSyncException", ex);
        ErrorDTO errorResponse = new ErrorDTO(400, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
}
