package com.example.trading_bot.indicators;


import com.example.trading_bot.config.AppProps;
import com.example.trading_bot.market.MultiTfBuffers;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FeatureAggregator {
    private final AppProps app;
    private final MultiTfBuffers buffers;
    private final IndicatorsService indicators;

    public MultiTfFeatures snapshot() {
        Map<String, FeatureFrame> map = new LinkedHashMap<>();
        for (String tf : app.getIntervals()) {
            var f = indicators.compute(buffers.buffer(tf).snapshot(), tf);
            if (f != null) map.put(tf, f);
        }
        return new MultiTfFeatures(app.getSymbol(), map);
    }
}
