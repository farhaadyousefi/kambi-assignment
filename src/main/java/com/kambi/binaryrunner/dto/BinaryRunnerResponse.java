package com.kambi.binaryrunner.dto;

import java.time.LocalDateTime;

public record BinaryRunnerResponse(LocalDateTime timestamp, String message, String[] details) {
}