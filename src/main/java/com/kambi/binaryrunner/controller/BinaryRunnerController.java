package com.kambi.binaryrunner.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.kambi.binaryrunner.dto.BinaryRunnerRequest;
import com.kambi.binaryrunner.dto.BinaryRunnerResponse;
import com.kambi.binaryrunner.service.BinaryRunnerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
@Slf4j
public class BinaryRunnerController {

        private final BinaryRunnerService service;

        @PostMapping("v1/runner")
        @Operation(summary = "running the executable file and bringing back its response")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "input binary has been executed succssfully", content = {
                                        @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))) }),
                        @ApiResponse(responseCode = "400", description = "bad request", content = {
                                        @Content(array = @ArraySchema(schema = @Schema(implementation = String.class))) }),
                        @ApiResponse(responseCode = "403", description = "Permission denied", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = BinaryRunnerResponse.class)) }),
                        @ApiResponse(responseCode = "404", description = "Command not found", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = BinaryRunnerResponse.class)) }),
                        @ApiResponse(responseCode = "408", description = "command execution timeout", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = BinaryRunnerResponse.class)) }),
                        @ApiResponse(responseCode = "500", description = "internal server error", content = {
                                        @Content(mediaType = "application/json", schema = @Schema(implementation = BinaryRunnerResponse.class)) })
        })
        public BinaryRunnerResponse binaryRunner(@Valid @RequestBody BinaryRunnerRequest request) {
                log.info("received request: {}", request.toString());
                return service.binaryRunner(request);
        }

}
