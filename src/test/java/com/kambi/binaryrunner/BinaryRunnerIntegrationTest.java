package com.kambi.binaryrunner;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kambi.binaryrunner.dto.BinaryRunnerRequest;

import static com.kambi.binaryrunner.BinaryRunnerServiceTest.DEFAULT_PATH;
import static com.kambi.binaryrunner.BinaryRunnerServiceTest.DEFAULT_EXTENTION;
import static com.kambi.binaryrunner.BinaryRunnerServiceTest.BINARY_FILES_WITH_EEROR;
import static com.kambi.binaryrunner.BinaryRunnerServiceTest.CORRECT_BINARY_FILE;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc
public class BinaryRunnerIntegrationTest {

    private final static List<String> args = new ArrayList<>();
    private final static String apiEndPint = "/api/v1/runner";

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setup() {
        args.add("-l");
    }

    @Test
    public void testRunningBinarySuccessfully() throws Exception {
        var absolutePath = DEFAULT_PATH + CORRECT_BINARY_FILE + DEFAULT_EXTENTION;
        var request = new BinaryRunnerRequest(absolutePath, args);
        var jsonContent = objectMapper.writeValueAsString(request);

        mockMvc.perform(MockMvcRequestBuilders.post(apiEndPint)
                .content(jsonContent)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    public void testExpectedToReturn4XX() throws Exception {
        var executorService = Executors.newFixedThreadPool(BINARY_FILES_WITH_EEROR.length);

        for (String file : BINARY_FILES_WITH_EEROR) {
            var absolutePath = DEFAULT_PATH + file + DEFAULT_EXTENTION;
            executorService.execute(() -> {
                try {
                    var request = new BinaryRunnerRequest(absolutePath, args);
                    var jsonContent = objectMapper.writeValueAsString(request);

                    mockMvc.perform(MockMvcRequestBuilders.post(apiEndPint)
                            .content(jsonContent)
                            .contentType(MediaType.APPLICATION_JSON))
                            .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                            .andExpect(jsonPath("$.details").exists());

                } catch (Exception e) {
                    fail("non Expected exception has been thrown");
                }
            });
        }
    }

}
