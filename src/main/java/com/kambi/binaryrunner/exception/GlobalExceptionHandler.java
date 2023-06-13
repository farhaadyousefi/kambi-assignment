package com.kambi.binaryrunner.exception;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.kambi.binaryrunner.dto.BinaryRunnerResponse;
import com.kambi.binaryrunner.model.CommandExitCode;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

	// handling validaiton related errors
	@Override
	protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
			HttpHeaders headers, HttpStatusCode status, WebRequest request) {

		var bindingResult = ex.getBindingResult();
		List<String> errors = new ArrayList<>();

		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			log.error("{}:{}", fieldError.getField(), fieldError.getDefaultMessage());
			errors.add(fieldError.getField() + ":" + fieldError.getDefaultMessage());
		}

		for (final ObjectError error : ex.getBindingResult().getGlobalErrors()) {
			log.error("{}:{}", error.getObjectName(), error.getDefaultMessage());
			errors.add(error.getObjectName() + ":" + error.getDefaultMessage());
		}

		String[] details = errors.stream().toArray(String[]::new);
		var errorResponse = new BinaryRunnerResponse(LocalDateTime.now(), "Validation failed",details);

		return handleExceptionInternal(ex, errorResponse, headers, HttpStatus.BAD_REQUEST, request);
	}

	@ExceptionHandler(value = { JsonMappingException.class })
	public ResponseEntity<Object> handleJacksonError(final JsonMappingException ex, final WebRequest request) {
		log.error("Cannot parse request. {}", ex.getMessage());

		String[] errors = new String[] { "please double check the json object" };
		var errorResponse = new BinaryRunnerResponse(LocalDateTime.now(), "not parsable request", errors);
		return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}

	// handling all errors related to running the executable file
	@ExceptionHandler(BinaryRunningException.class)
	public final ResponseEntity<BinaryRunnerResponse> handleRunningFileException(Exception ex, WebRequest request) {
		var exitCode = CommandExitCode.getCommandExitCodeByValue(Integer.valueOf(ex.getMessage()));
		log.error("exception has been occured during runningt the binary file\n {}", exitCode.getErrorMessage());

		String[] errors = new String[] { exitCode.getErrorMessage() };
		var errorResponse = new BinaryRunnerResponse(LocalDateTime.now(), "error during running the binary file",
				errors);
		return new ResponseEntity<>(errorResponse, exitCode.getHttpstatus());
	}

	// handling the rest of exceptions
	@ExceptionHandler(Exception.class)
	public final ResponseEntity<Object> handleRemainExceptions(Exception ex, WebRequest request) {
		log.error("exception {} has been occured", ex.getMessage(), ex);
		String[] errors = new String[] { "please contact the admin" };
		var errorResponse = new BinaryRunnerResponse(LocalDateTime.now(), "Internal server error", errors);
		return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
