package io.movetodata.snap.sharedutils.Response;

import com.esotericsoftware.minlog.Log;
import io.movetodata.snap.passport.exception.UnauthorizedException;
import io.movetodata.snap.sharedutils.Exceptions.BadRequestException;
import io.movetodata.snap.sharedutils.Exceptions.EnvConfigurationException;
import io.movetodata.snap.sharedutils.Exceptions.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.NoSuchElementException;

//@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j(topic = "GLOBAL_EXCEPTION_HANDLER")
@ControllerAdvice
@Transactional
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        Log.error("handleMethodArgumentNotValid", ex);

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex,
                                                         HttpHeaders headers,
                                                         HttpStatus status,
                                                         WebRequest request) {
        Log.error("handleBindException", ex);

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status,
                                                                          WebRequest request) {
        Log.error("handleMissingServletRequestParameter", ex);

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex,
                                                                          HttpHeaders headers,
                                                                          HttpStatus status,
                                                                          WebRequest request) {
        Log.error("handleServletRequestBindingException", ex);

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex,
                                                        HttpHeaders headers,
                                                        HttpStatus status,
                                                        WebRequest request) {
        Log.error("handleTypeMismatch", ex);
        Log.error("handleTypeMismatch headers", headers.toString());
        Log.error("handleTypeMismatch request", request.toString());
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Request params doesn't match", "These frontend folks must think I am Gandalf with a keyboard!😄🪄, not a computer!");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Log.error("handleHttpMessageNotReadable", ex);
        Log.error("headers", headers.toString());
        Log.error("request", request.toString());

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        Log.error("handleMissingServletRequestPart", ex);

        ErrorDTO errorResponse = new ErrorDTO(400, "ARGUMENT INVALID");
        return ResponseEntity.status(400).body(errorResponse);
    }

    @ExceptionHandler(value = {NoSuchElementException.class})
    protected ResponseEntity<ErrorDTO> handleNoSuchElementException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.NOT_FOUND.value(), "No Such File!", "Either this file does not exist or has been deleted.");
        Log.error("handleNoSuchElementException", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value = {HttpServerErrorException.InternalServerError.class})
    protected ResponseEntity<ErrorDTO> handleInternalServerError(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error!", "Server got stage fright! 🎭 We're working on its performance anxiety. 🛠️😅");
        Log.error("handleInternalServerError", ex);
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(value = {UnsupportedOperationException.class})
    protected ResponseEntity<ErrorDTO> handleUnsupportedOperationException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.PRECONDITION_FAILED.value(), "Unsupported operation!", ex.getMessage());

        Log.error("handleUnsupportedOperationException", ex);
        return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED).body(errorResponse);
    }

    @ExceptionHandler(value = UnauthorizedException.class)
    protected ResponseEntity<ErrorDTO> handleUnauthorizedException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.UNAUTHORIZED.value(), "Unauthorized!", ex.getMessage());
        Log.error("handleUnauthorizedException", ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    @ExceptionHandler(value = RuntimeException.class)
    protected ResponseEntity<ErrorDTO> handleRuntimeException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), "Something went wrong!", ex.getMessage());
        Log.error("handleRuntimeException", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = BadRequestException.class)
    protected ResponseEntity<ErrorDTO> BadRequestException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        Log.error("BadRequestException", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(value = ResourceNotFoundException.class)
    protected ResponseEntity<ErrorDTO> ResourceNotFoundException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.NOT_FOUND.value(), ex.getMessage());
        Log.error("ResourceNotFoundException", ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(value = EnvConfigurationException.class)
    protected ResponseEntity<ErrorDTO> EnvConfigurationException(Exception ex) {
        ErrorDTO errorResponse = new ErrorDTO(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        Log.error("EnvConfigurationException", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}