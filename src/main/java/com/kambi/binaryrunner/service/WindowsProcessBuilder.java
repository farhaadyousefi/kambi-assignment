package com.kambi.binaryrunner.service;


import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class WindowsProcessBuilder implements CommandExecutorBuilderStrategy{

    @Override
    public ProcessBuilder commandExecuterBuilder(boolean runWithSuperUser, List<String> commands) {
        //run with super user privilage
        if(runWithSuperUser){
            log.info("start running cmd.exe /c {} {}", WINDOWS_SUPERUSER_COMMAND, commands.toString());
            List<String> finalCommand = new ArrayList<>();
            finalCommand.add("cmd.exe /c");
            finalCommand.add(WINDOWS_SUPERUSER_COMMAND);
            finalCommand.addAll(commands);

            return new ProcessBuilder(finalCommand);
        }
        
        log.info("start running {}",commands.toString());
        return new ProcessBuilder(commands);
    }

    
}
