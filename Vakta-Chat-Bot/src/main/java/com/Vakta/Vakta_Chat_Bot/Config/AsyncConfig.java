package com.Vakta.Vakta_Chat_Bot.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync // This annotation is the key to enabling background tasks
public class AsyncConfig {
    // This class can be empty. Its only purpose is to enable the @Async feature.
}