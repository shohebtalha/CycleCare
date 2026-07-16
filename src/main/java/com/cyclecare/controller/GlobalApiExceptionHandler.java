package com.cyclecare.controller;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handles exceptions thrown by {@link RestController} classes (all /api/** endpoints).
 * Returns RFC 7807 ProblemDetail JSON — never HTML.
 *
 * MVC page-rendering controllers are handled separately by {@link GlobalExceptionHandler}.
 */
@RestControllerAdvice(annotations = RestController.class)
public class GlobalApiExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalApiExceptionHandler.class);

    /**
     * Bean Validation failure on a @RequestBody — e.g. blank question sent to the AI assistant.
     * Returns 400 with a field-level error map so clients know exactly which field failed.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidationErrors(MethodArgumentNotValidException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Validation failed");

        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        problem.setProperty("errors", fieldErrors);
        return problem;
    }

    /**
     * Constraint violations from method-level @Validated — e.g. path variable out of range.
     * Returns 400 with a plain message.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Constraint violation");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Malformed or unreadable JSON body — e.g. missing closing brace, wrong field type.
     * Returns 400 with a safe generic message (no internal class names leaked).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableBody(HttpMessageNotReadableException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Malformed request body");
        problem.setDetail("The request body could not be read. Check that the JSON is valid and all required fields are present.");
        return problem;
    }

    /**
     * Wrong type for a path variable or request parameter — e.g. letters where a Long is expected.
     * Returns 400 with a descriptive message.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid parameter");
        problem.setDetail("Parameter '" + ex.getName() + "' could not be converted to the expected type.");
        return problem;
    }

    /**
     * Business rule violations thrown deliberately by services — e.g. duplicate entry,
     * invalid cycle date, missing required data.
     * Returns 400 because the error is the caller's responsibility.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problem.setTitle("Invalid request");
        problem.setDetail(ex.getMessage());
        return problem;
    }

    /**
     * Application state errors thrown deliberately by services — e.g. configuration not ready.
     * Returns 503 because the server is not able to handle the request right now.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        logger.error("Application state error on API request", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Service temporarily unavailable");
        problem.setDetail("The server encountered a configuration issue. Please try again later.");
        return problem;
    }

    /**
     * Database access failure — e.g. connection timeout, constraint violation at DB level.
     * Returns 503. Internal DB details are never exposed to the caller.
     */
    @ExceptionHandler(DataAccessException.class)
    public ProblemDetail handleDataAccess(DataAccessException ex) {
        logger.error("Data access error on API request", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        problem.setTitle("Data access error");
        problem.setDetail("A database error occurred. Please try again later.");
        return problem;
    }

    /**
     * Spring Security blocked the request — should not normally reach here because Security
     * handles 403 itself, but included as a safety net.
     * Re-throws so Spring Security can handle it properly via its own filter chain.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) throws AccessDeniedException {
        throw ex;
    }

    /**
     * Catch-all for any unexpected exception not covered above.
     * Returns 500 with a generic message. The full stack trace is logged server-side only.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        logger.error("Unexpected error on API request", ex);
        ProblemDetail problem = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problem.setTitle("Unexpected error");
        problem.setDetail("An unexpected error occurred. Please try again later.");
        return problem;
    }
}
