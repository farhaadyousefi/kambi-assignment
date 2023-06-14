package com.kambi.binaryrunner.model;

import java.util.Arrays;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommandExitCode {
    SUCCESSFUL("input binary has been executed succssfully", 0, HttpStatus.OK),
    INVALID_ARG("either the mandatory arg is absent or inputted one is not correct", 1, HttpStatus.BAD_REQUEST),
    INVALID_OPTION("either the mandatory option is absent or inputted one is not correct", 2, HttpStatus.BAD_REQUEST),
    INSUFFICIENT_ACCESS_RIGHTS("Permission denied", 4, HttpStatus.FORBIDDEN),
    INACCESSIBLE_RESOURCES("missing or inaccessible file or resource", 3, HttpStatus.BAD_REQUEST),
    FILE_NOT_FOUND("execution file not found", 98, HttpStatus.NOT_FOUND),
    TIMEOUT_REACHED("command execution reached the defined timeout and execution has been stopped", 99, HttpStatus.REQUEST_TIMEOUT),
    FILE_PERMISSION_DENIED("Permission denied", 13, HttpStatus.FORBIDDEN),
    COMMAND_PERMISSION_DENIED("Permission denied", 126, HttpStatus.FORBIDDEN),
    COMMAND_NOT_FOUND("Command not found-please check the command and its options or arguments", 127, HttpStatus.NOT_FOUND),    
    NOT_COMPATIBLE_WITH_OS("input file is not compatible with os", 193, HttpStatus.BAD_REQUEST),    
    INTERNAL_SERVER_ERROR("internal server error", 500, HttpStatus.INTERNAL_SERVER_ERROR); //for the rest of exit code

    private final String errorMessage;
    private final int exitCode;
    private final HttpStatusCode httpstatus;

    public static CommandExitCode getCommandExitCodeByValue(int value) {
        return Arrays.stream(CommandExitCode.values())
            .filter(cmdCode -> cmdCode.exitCode == value)
            .findFirst().orElse(INTERNAL_SERVER_ERROR);
    }

    public static String getStringHttpStatus(CommandExitCode commandExitCode) {
        return String.valueOf(commandExitCode.getHttpstatus());
    }
}