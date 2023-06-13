package com.kambi.binaryrunner.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kambi.binaryrunner.dto.BinaryRunnerRequest;
import com.kambi.binaryrunner.dto.BinaryRunnerResponse;
import com.kambi.binaryrunner.exception.BinaryRunningException;
import com.kambi.binaryrunner.model.BinaryRunnerResult;

import lombok.extern.slf4j.Slf4j;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.kambi.binaryrunner.model.CommandExitCode.SUCCESSFUL;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_NOT_FOUND;
import static com.kambi.binaryrunner.model.CommandExitCode.TIMEOUT_REACHED;
import static com.kambi.binaryrunner.model.CommandExitCode.INTERNAL_SERVER_ERROR;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_PERMISSION_DENIED;

@Slf4j
@Service
public class BinaryRunnerService {
    // using three underlines for separating the command, options, and arguments.
    // there is almost no command that contains 3 underlines
    private static final String UNDERLINES = "___";

    @Value("${running.process.timeout}")
    private long processTimeout;

    public BinaryRunnerResponse binaryRunner(BinaryRunnerRequest request) throws BinaryRunningException {
        var command = setCommand(request);
        var binaryRunnerResult = executeBinary(command.split(UNDERLINES));
        var message = "the binary file execution result";
        if (binaryRunnerResult.exitCode() == SUCCESSFUL.getExitCode()) {
            String[] details = binaryRunnerResult.output().stream().toArray(String[]::new);
            return new BinaryRunnerResponse(LocalDateTime.now(), message, details);

        }
        throw new BinaryRunningException(String.valueOf(binaryRunnerResult.exitCode()));
    }

    private String setCommand(BinaryRunnerRequest request) {
        String fullPath = setFullPathOfFile(request.getBinaryFile().trim());
        var arguments = setAreguments(request.getArguments());

        return fullPath + arguments;
    }

    /*
     * validating the existence of executable file.
     * if fileName has no path, the application will check the current working
     * directory and the user's home directory respectively.
     */
    private String setFullPathOfFile(String fileName) throws BinaryRunningException {
        var slash = "/";
        var isFileExist = new File(fileName).exists();

        if (isFileExist) {
            log.info("file has been found {}", fileName);
            return fileName;
        } else if (fileName.contains(slash)) { // inputed file contain invalid path
            throw new BinaryRunningException(String.valueOf(FILE_NOT_FOUND.getExitCode()));
        }

        // check for working and user_home directory
        var workingDirectory = Paths.get("").toAbsolutePath().toString() + slash;
        var userHome = System.getProperty("user.home") + slash;
        String[] otherPath = new String[] { workingDirectory, userHome };

        for (String path : otherPath) {
            var file = new File(path + fileName);
            if (file.exists()) {
                log.info("full path of excutable file is {}{}", path, fileName);
                return path + fileName;
            }
        }

        // throw exception if file doesn't exist in none of the above path
        throw new BinaryRunningException(String.valueOf(FILE_NOT_FOUND.getExitCode()));
    }

    // setting all passed option(s) and argument(s)
    private String setAreguments(List<String> arguments) {
        if (arguments == null || arguments.isEmpty()) {
            return "";
        }

        var argsBuilder = new StringBuilder();
        for (String arg : arguments) {
            argsBuilder.append(UNDERLINES).append(arg.trim());
        }
        return argsBuilder.toString();
    }

    private BinaryRunnerResult executeBinary(String[] command) {
        log.info("start running {}", Arrays.toString(command));
        var executorService = Executors.newSingleThreadExecutor();
        long startTime = System.currentTimeMillis();
        try {
            // running the command with the help of an auxiliary thread
            CompletableFuture<BinaryRunnerResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    var processBuilder = new ProcessBuilder(command);
                    var process = processBuilder.start();

                    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    List<String> output = new ArrayList<>();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.add(line);
                    }
                    return new BinaryRunnerResult(process.waitFor(), output);
                } catch (IOException ex) {
                    log.error("exception during the execution of file {}", ex.getMessage());
                    var errorMessage = ex.getMessage().toLowerCase();
                    // the file is not executable or has no sufficient privileges
                    String[] permissionDenied = new String[] { "permission denied", "error=13" };

                    if (Arrays.stream(permissionDenied).anyMatch(errorMessage::contains)) {
                        return new BinaryRunnerResult(FILE_PERMISSION_DENIED.getExitCode(), null);
                    }
                    return new BinaryRunnerResult(INTERNAL_SERVER_ERROR.getExitCode(), null);
                } catch (InterruptedException e) {
                    log.error("exception during the execution of file {}", e.getMessage());
                    return new BinaryRunnerResult(INTERNAL_SERVER_ERROR.getExitCode(), null);
                }
            }, executorService);

            try {
                var result = future.get(processTimeout, TimeUnit.MILLISECONDS);
                log.info("excution of {} finished by exit code {}", command, result.exitCode());
                return result;
            } catch (TimeoutException e) {
                log.error("execution timed out and has been cancelled");
                // Cancel the task and release its responsible thread if timeout is reached
                future.cancel(true);
                return new BinaryRunnerResult(TIMEOUT_REACHED.getExitCode(), null);

            } catch (InterruptedException | ExecutionException e) {
                log.error("exception during the execution of file {}", e.getMessage(), e);
                return new BinaryRunnerResult(INTERNAL_SERVER_ERROR.getExitCode(), null);
            }

        } finally

        {
            long endTime = System.currentTimeMillis();
            long executionTime = endTime - startTime;
            log.info("commnad execution time is {} milliseconds", executionTime);
            executorService.shutdownNow();
        }
    }
}
