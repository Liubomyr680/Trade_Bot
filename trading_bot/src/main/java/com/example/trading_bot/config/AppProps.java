package com.example.trading_bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppProps {
    private String symbol;
    private List<String> intervals;
    private int barsBuffer = 300;
    private int historyLoad = 100;

}
