package com.kambi.binaryrunner.service;

import java.util.List;

public interface CommandExecutorBuilderStrategy {
    public final static String WINDOWS_SUPERUSER_COMMAND = "runas /user:administrator";
    public final static String UNIX_BASE_SUPERUSER_COMMAND = "sudo";

    ProcessBuilder commandExecuterBuilder(boolean runWithSuperUser, List<String> commands);
}
