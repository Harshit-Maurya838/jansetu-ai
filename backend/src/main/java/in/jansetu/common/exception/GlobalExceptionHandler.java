package in.jansetu.common.exception;

import in.jansetu.common.response.ApiError;
import in.jansetu.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ApiResponse<Void>> handleApi(ApiException e, HttpServletRequest req) {
        return body(e.getStatus(), e.getCode(), e.getMessage(), req, List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        var fields = e.getBindingResult().getFieldErrors().stream()
                .map(f -> new ApiError.FieldError(f.getField(), f.getDefaultMessage()))
                .toList();
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Request validation failed", req, fields);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
    ResponseEntity<ApiResponse<Void>> handleUnreadable(Exception e, HttpServletRequest req) {
        return body(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", "Malformed request", req, List.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ApiResponse<Void>> handleConflict(OptimisticLockingFailureException e, HttpServletRequest req) {
        return body(HttpStatus.CONFLICT, "VERSION_CONFLICT", "The record was changed by someone else. Try again", req, List.of());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiResponse<Void>> handleDenied(AccessDeniedException e, HttpServletRequest req) {
        return body(HttpStatus.FORBIDDEN, "FORBIDDEN", "You do not have permission for this action", req, List.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiResponse<Void>> handleOther(Exception e, HttpServletRequest req) {
        log.error("Unhandled error on {}", req.getRequestURI(), e);
        // Never leak internals (or health data) in error messages.
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Something went wrong", req, List.of());
    }

    private ResponseEntity<ApiResponse<Void>> body(HttpStatus status, String code, String msg,
                                                   HttpServletRequest req, List<ApiError.FieldError> fields) {
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(new ApiError(code, msg, req.getRequestURI(), fields)));
    }
}
