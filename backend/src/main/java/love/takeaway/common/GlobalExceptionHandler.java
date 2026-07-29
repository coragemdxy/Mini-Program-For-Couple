package love.takeaway.common;

import jakarta.persistence.OptimisticLockException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ApiException.class)
    ResponseEntity<ProblemDetail> handleApiException(ApiException exception) {
        return problem(exception.getStatus(), exception.getCode(), exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", detail);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ProblemDetail> handleConstraintViolation(ConstraintViolationException exception) {
        return problem(HttpStatus.BAD_REQUEST, "VALIDATION_FAILED", exception.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException exception) {
        return problem(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "当前微信账号没有权限执行此操作");
    }

    @ExceptionHandler({OptimisticLockException.class, DataIntegrityViolationException.class})
    ResponseEntity<ProblemDetail> handleConflict(Exception exception) {
        return problem(HttpStatus.CONFLICT, "CONCURRENT_MODIFICATION", "数据已发生变化，请刷新后重试");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception exception) {
        log.error("Unexpected API error", exception);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "服务器暂时无法处理请求");
    }

    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(code);
        problem.setType(URI.create("https://takeaway.local/problems/" + code.toLowerCase()));
        problem.setProperty("code", code);
        return ResponseEntity.status(status).body(problem);
    }
}

