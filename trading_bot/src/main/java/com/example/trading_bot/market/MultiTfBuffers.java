package com.example.trading_bot.market;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.config.AppProps;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiTfBuffers {
    private final AppProps app;
    private final FuturesRest rest;

    private final Map<String, TfCandleBuffer> byTf = new LinkedHashMap<>();

    public void initAll() {
        byTf.clear();
        for (String tf : app.getIntervals()) {
            TfCandleBuffer b = new TfCandleBuffer(app.getSymbol(), tf, app.getBarsBuffer());
            JsonNode arr = rest.klines(app.getSymbol(), tf, app.getHistoryLoad());
            b.initFromKlines(arr);
            byTf.put(tf, b);
        }
    }

    public TfCandleBuffer buffer(String tf) { return byTf.get(tf); }
    public Map<String, TfCandleBuffer> all() { return byTf; }
}