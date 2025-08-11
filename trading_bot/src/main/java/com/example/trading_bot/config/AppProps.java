package com.example.trading_bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProps {
    private String symbol;
    private String interval;
    private int barsBuffer = 300;
    private boolean arm = false;

}
