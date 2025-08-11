package com.example.trading_bot.indicators;

import java.util.Map;

public record MultiTfFeatures(
        String symbol,
        Map<String, FeatureFrame> frames // ключ: "1m","5m","15m","1h"
) {}
