package com.kambi.binaryrunner.model;

import java.util.List;

public record BinaryRunnerResult(int exitCode, List<String> output) {
}
