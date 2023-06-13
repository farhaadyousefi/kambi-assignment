package com.kambi.binaryrunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

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
    public final static String DEFAULT_PATH;
    public final static String DEFAULT_EXTENTION;
    public final static String CORRECT_BINARY_FILE = "correct";
    public final static String[] BINARY_FILES_WITH_EEROR = new String[] { "no_permission", "wrong_arg", "timeout",
            "wrong_command" };

    static {
        var os = System.getProperty("os.name").toLowerCase();
        if (os.contains("windows")) {
            DEFAULT_PATH = "src/test/resources/binaryFiles/windows/";
            DEFAULT_EXTENTION = ".bat";
        } else {
            DEFAULT_PATH = "src/test/resources/binaryFiles/unixBase/";
            DEFAULT_EXTENTION = ".sh";
        }

    }

    @Test
    void testSuccessfullyRunCommand() {
        var fileName = DEFAULT_PATH + CORRECT_BINARY_FILE + DEFAULT_EXTENTION;
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
                Arguments.of(DEFAULT_PATH + BINARY_FILES_WITH_EEROR[0] + DEFAULT_EXTENTION,
                        (Object) new String[] { "../resources", "-l" },
                        FILE_PERMISSION_DENIED.getExitCode()),
                Arguments.of(DEFAULT_PATH + BINARY_FILES_WITH_EEROR[1] + DEFAULT_EXTENTION,
                        (Object) new String[] { "../resources", "-wrong_arg" },
                        INVALID_ARG.getExitCode()),
                Arguments.of(DEFAULT_PATH + BINARY_FILES_WITH_EEROR[2] + DEFAULT_EXTENTION,
                        (Object) new String[] { "../resources", "-l" },
                        TIMEOUT_REACHED.getExitCode()),
                Arguments.of("wrong/path/ls.sh",
                        (Object) new String[] { "-l" },
                        FILE_NOT_FOUND.getExitCode()),
                Arguments.of(DEFAULT_PATH + BINARY_FILES_WITH_EEROR[3] + DEFAULT_EXTENTION,
                        (Object) new String[] { "-l" },
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

        assertThrows(BinaryRunningException.class, () -> {
            binaryRunnerService.binaryRunner(request);
        });
    }
}
