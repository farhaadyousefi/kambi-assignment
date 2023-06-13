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

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import static com.kambi.binaryrunner.model.CommandExitCode.SUCCESSFUL;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_NOT_FOUND;
import static com.kambi.binaryrunner.model.CommandExitCode.TIMEOUT_REACHED;
import static com.kambi.binaryrunner.model.CommandExitCode.INTERNAL_SERVER_ERROR;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_PERMISSION_DENIED;
import static com.kambi.binaryrunner.service.CommandExecutorBuilderStrategy.UNIX_BASE_SUPERUSER_COMMAND;
import static com.kambi.binaryrunner.service.CommandExecutorBuilderStrategy.WINDOWS_SUPERUSER_COMMAND;;

@Slf4j
@Service
@RequiredArgsConstructor
public class BinaryRunnerService {
    private final CommandExecutorBuilderStrategy commandExecutorBuilder;

    @Value("${running.process.timeout}")
    private long processTimeout;

    private boolean runBySuperUser = false;

    /*
     * 1.validate the binary file, if requested to run with the
     * superuser privilege, first, we should reach the binary file path anmd name 
     * strategy design pattern has been used to create command runner based on the
     * operation system
     */
    public BinaryRunnerResponse binaryRunner(BinaryRunnerRequest request) throws BinaryRunningException {
        // reaching the binary file
        var binaryFile = binaryFileSeperator(request.getBinaryFile().trim());

        // binary file validaiton and set the command with its args
        List<String> commands = setCommand(binaryFile, request.getArguments());

        // create command runner builder based on os
        var processBuilder = commandExecutorBuilder.commandExecuterBuilder(runBySuperUser, commands);

        var binaryRunnerResult = executeBinary(processBuilder);
        var message = "the binary file execution result";
        if (binaryRunnerResult.exitCode() == SUCCESSFUL.getExitCode()) {
            String[] details = binaryRunnerResult.output().stream().toArray(String[]::new);
            return new BinaryRunnerResponse(LocalDateTime.now(), message, details);

        }
        throw new BinaryRunningException(String.valueOf(binaryRunnerResult.exitCode()));
    }

    /*
     * separating the run by super user command from the real binary file name and path.
     */
    private String binaryFileSeperator(String binaryFile) {
        var os = System.getProperty("os.name").toLowerCase();

        if (os.contains("windows") && binaryFile.toLowerCase().startsWith(WINDOWS_SUPERUSER_COMMAND)) {
            runBySuperUser = true;
            return binaryFile.substring(WINDOWS_SUPERUSER_COMMAND.length()).trim();
        } else if (binaryFile.toLowerCase().startsWith(UNIX_BASE_SUPERUSER_COMMAND)) {
            runBySuperUser = true;
            return binaryFile.substring(UNIX_BASE_SUPERUSER_COMMAND.length()).trim();
        }
        return binaryFile;
    }

    /*
     * validating the existence of executable file.
     * if fileName has no path, the application will check the current working
     * directory and the user's home directory respectively (has been handled by
     * setFullPathOfFile()).
     * 
     * if the command should run by the admin or root user, for path and binary file
     * validaiotn first we should remove for example sudo keyword (has been handled
     * by checkRunAsAdmin()).
     */
    private List<String> setCommand(String binaryFile, List<String> args) {
        String binaryFileWithFullPath = setFullPathOfFile(binaryFile);
        List<String> finalCommand = new ArrayList<>();

        finalCommand.add(binaryFileWithFullPath);
        if (args != null && !args.isEmpty()) {
            finalCommand.addAll(args);
        }

        return finalCommand;
    }

    private String setFullPathOfFile(String binaryFile) throws BinaryRunningException {

        var slash = "/";
        var isFileExist = new File(binaryFile).exists();

        if (isFileExist) {
            log.info("file has been found {}", binaryFile);
            return binaryFile;
        } else if (binaryFile.contains(slash)) { // inputed file contain invalid path
            throw new BinaryRunningException(String.valueOf(FILE_NOT_FOUND.getExitCode()));
        }

        // check for working and user_home directory
        var workingDirectory = Paths.get("").toAbsolutePath().toString() + slash;
        var userHome = System.getProperty("user.home") + slash;
        String[] otherPath = new String[] { workingDirectory, userHome };

        for (String path : otherPath) {
            var file = new File(path + binaryFile);
            if (file.exists()) {
                log.info("full path of excutable file is {}{}", path, binaryFile);
                return path + binaryFile;
            }
        }

        // throw exception if file doesn't exist in none of the above path
        throw new BinaryRunningException(String.valueOf(FILE_NOT_FOUND.getExitCode()));
    }

    private BinaryRunnerResult executeBinary(ProcessBuilder processBuilder) {
        var executorService = Executors.newSingleThreadExecutor();
        long startTime = System.currentTimeMillis();
        try {
            // running the command with the help of an auxiliary thread
            CompletableFuture<BinaryRunnerResult> future = CompletableFuture.supplyAsync(() -> {
                try {
                    var process = processBuilder.start();

                    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    List<String> output = new ArrayList<>();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.add(line);
                    }
                    return new BinaryRunnerResult(process.waitFor(), output);
                } catch (IOException ex) {
                    var errorMessage = ex.getMessage().toLowerCase();
                    // the file is not executable or has no sufficient privileges
                    String[] permissionDenied = new String[] { "permission denied", "error=13" };
                    if (Arrays.stream(permissionDenied).anyMatch(errorMessage::contains)) {
                        return new BinaryRunnerResult(FILE_PERMISSION_DENIED.getExitCode(), null);
                    }

                    log.error("exception during the execution of file {}", ex.getMessage(), ex);
                    return new BinaryRunnerResult(INTERNAL_SERVER_ERROR.getExitCode(), null);
                } catch (InterruptedException e) {
                    log.error("exception during the execution of file {}", e.getMessage());
                    return new BinaryRunnerResult(INTERNAL_SERVER_ERROR.getExitCode(), null);
                }
            }, executorService);

            try {
                var result = future.get(processTimeout, TimeUnit.MILLISECONDS);
                log.info("excution finished by exit code {}", result.exitCode());
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
