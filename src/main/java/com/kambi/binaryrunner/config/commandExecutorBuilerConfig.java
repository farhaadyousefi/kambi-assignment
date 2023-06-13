package com.kambi.binaryrunner.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kambi.binaryrunner.service.CommandExecutorBuilderStrategy;
import com.kambi.binaryrunner.service.UnixBaseProcessBuilder;
import com.kambi.binaryrunner.service.WindowsProcessBuilder;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class commandExecutorBuilerConfig {

    @Bean(name = "commandExecutorBuilder")
    CommandExecutorBuilderStrategy commandExecutorBuilder() {
        var os = System.getProperty("os.name").toLowerCase();
        log.info("operation system is {}", os);

        if (os.contains("windows")) {
            return new WindowsProcessBuilder();
        } else {
            return new UnixBaseProcessBuilder();
        }
    }

}
