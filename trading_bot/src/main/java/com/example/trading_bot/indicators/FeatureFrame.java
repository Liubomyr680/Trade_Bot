package com.example.trading_bot.indicators;

public record FeatureFrame(
        double close, double ema20, double ema50,
        double rsi14, double macd, double macdSignal, double macdHist,
        double atrPct
) {}
