package com.example.trading_bot.market;

public record Candle(
        long openTimeMs,
        double open,
        double high,
        double low,
        double close,
        double volume
) {}
