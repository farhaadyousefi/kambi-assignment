package com.kambi.binaryrunner.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class BinaryRunnerRequest {

    @NotNull
    @Pattern(regexp = "^.*\\.(sh|bat)$", message = "inputted file should be an executable")
    @Schema(description = "it should end with .sh", example = "/command/ls.sh")
    private final String binaryFile;

    @Schema(description = "an array of all option(s) and argument(s) related to the executable binary file", example = "[/app/, -l]")
    private List<String> arguments;

    @Override
    public String toString() {
        if (this.arguments == null || this.arguments.isEmpty()) {
            return "binaryFile=" + binaryFile;
        }
        return "binaryFile=" + binaryFile + "," + "arguments=" + this.arguments.toString();
    }

}
