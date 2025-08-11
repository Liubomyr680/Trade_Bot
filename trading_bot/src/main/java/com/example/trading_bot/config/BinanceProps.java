package com.example.trading_bot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "binance")
public class BinanceProps {
    private String restBase;
    private String wsStreamBase;
    private int recvWindowMs = 5000;
    public String getRestBase() { return restBase; }
    public void setRestBase(String restBase) { this.restBase = restBase; }
    public String getWsStreamBase() { return wsStreamBase; }
    public void setWsStreamBase(String wsStreamBase) { this.wsStreamBase = wsStreamBase; }
    public int getRecvWindowMs() { return recvWindowMs; }
    public void setRecvWindowMs(int recvWindowMs) { this.recvWindowMs = recvWindowMs; }
}
