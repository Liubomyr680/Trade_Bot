package com.example.trading_bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "binance")
public class BinanceProps {
    private String restBase;
    private String wsStreamBase;
    private int recvWindowMs = 5000;

}
