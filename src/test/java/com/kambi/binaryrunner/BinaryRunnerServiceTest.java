package com.kambi.binaryrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.kambi.binaryrunner.dto.BinaryRunnerRequest;
import com.kambi.binaryrunner.exception.BinaryRunningException;
import com.kambi.binaryrunner.service.BinaryRunnerService;

import static com.kambi.binaryrunner.model.CommandExitCode.INVALID_ARG;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_NOT_FOUND;
import static com.kambi.binaryrunner.model.CommandExitCode.TIMEOUT_REACHED;
import static com.kambi.binaryrunner.model.CommandExitCode.COMMAND_NOT_FOUND;
import static com.kambi.binaryrunner.model.CommandExitCode.FILE_PERMISSION_DENIED;

@SpringBootTest(properties = "running.process.timeout=5000")
class BinaryRunnerServiceTest {

    @Autowired
    private BinaryRunnerService binaryRunnerService;

    public final static String DEFAULT_PATH = "src/test/resources/binaryFiles/";

    @Test
    void testSuccessfullyRunCommand() {
        var fileName = DEFAULT_PATH + "ls.sh";
        var file = new File(fileName);
        var absolutePath = file.getAbsolutePath();

        List<String> args = new ArrayList<>();
        args.add("-l");

        var request = new BinaryRunnerRequest(absolutePath, args);
        var result = binaryRunnerService.binaryRunner(request);

        assertNotNull(result);
    }

    /*
     * provide arguments for ParameterizedTest that expected to raise a
     * BinaryRunningException exception
     * param one: executable file
     * param two: array of command options or arguments
     * param three: expected exit code
     */
    private static Stream<Arguments> ArgumntsProvider() {
        return Stream.of(
                Arguments.of(DEFAULT_PATH + "no_permission.sh", (Object) new String[] { "../resources", "-l" },
                        FILE_PERMISSION_DENIED.getExitCode()),
                Arguments.of(DEFAULT_PATH + "wrong_arg.sh", (Object) new String[] { "../resources", "-wrong_arg" },
                        INVALID_ARG.getExitCode()),
                Arguments.of(DEFAULT_PATH + "timeout_ls.sh", (Object) new String[] { "../resources", "-l" },
                        TIMEOUT_REACHED.getExitCode()),
                Arguments.of("wrong/path/ls.sh", (Object) new String[] { "-l" }, FILE_NOT_FOUND.getExitCode()),
                Arguments.of(DEFAULT_PATH + "wrong_command.sh", (Object) new String[] { "-l" },
                        COMMAND_NOT_FOUND.getExitCode()));
    }

    @ParameterizedTest
    @MethodSource("ArgumntsProvider")
    void testThrowFileNotFoundRelatedException(String fileWithPath, String[] arguments, int expected) {
        var file = new File(fileWithPath);
        var absolutePath = file.getAbsolutePath();

        List<String> args = new ArrayList<>();
        Collections.addAll(args, arguments);

        var request = new BinaryRunnerRequest(absolutePath, args);

        var exception = assertThrows(BinaryRunningException.class, () -> {
            binaryRunnerService.binaryRunner(request);
        });

        var actual = exception.getMessage();
        assertEquals(String.valueOf(expected), actual);
    }
}
