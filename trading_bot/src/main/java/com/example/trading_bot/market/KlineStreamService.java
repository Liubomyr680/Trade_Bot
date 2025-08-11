package com.example.trading_bot.market;

import com.example.trading_bot.config.AppProps;
import com.example.trading_bot.config.BinanceProps;
import com.example.trading_bot.indicators.FeatureFrame;
import com.example.trading_bot.indicators.IndicatorsService;
import com.example.trading_bot.ws.WsClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KlineStreamService {
    private final AppProps app;
    private final BinanceProps cfg;
    private final MultiTfBuffers buffers;
    private final IndicatorsService indicators;

    private final Map<String, WsClient> clients = new LinkedHashMap<>();

    /** запуск WS для всіх TF */
    public void startAll() {
        for (String tf : app.getIntervals()) start(tf);
    }

    private void start(String tf) {
        String stream = app.getSymbol().toLowerCase() + "@kline_" + tf;
        String url = cfg.getWsStreamBase() + "/ws/" + stream;
        WsClient ws = new WsClient(url, msg -> onMsg(tf, msg));
        clients.put(tf, ws);
        log.info("Subscribed to {}", stream);
    }

    private void onMsg(String tf, JsonNode root) {
        JsonNode k = root.path("k");
        if (k.isMissingNode() || !k.path("x").asBoolean(false)) return; // лише закриті бари

        Candle c = new Candle(
                k.get("t").asLong(),
                k.get("o").asDouble(),
                k.get("h").asDouble(),
                k.get("l").asDouble(),
                k.get("c").asDouble(),
                k.get("v").asDouble()
        );
        TfCandleBuffer buf = buffers.buffer(tf);
        buf.push(c);

        FeatureFrame f = indicators.compute(buf.snapshot(), tf);
        if (f != null) {
            log.info("IND {} {} | C={} EMA20={} EMA50={} RSI14={} MACD={} Sig={} Hist={} ATR%={}",
                    app.getSymbol(), tf,
                    r2(f.close()), r2(f.ema20()), r2(f.ema50()),
                    r2(f.rsi14()), r3(f.macd()), r3(f.macdSignal()),
                    r3(f.macdHist()), r3(f.atrPct()));
            // TODO: передати у GPT (пізніше)
        }
    }

    private double r2(double v){ return Math.round(v*100.0)/100.0; }
    private double r3(double v){ return Math.round(v*1000.0)/1000.0; }

    @PreDestroy
    public void stopAll() {
        clients.values().forEach(WsClient::close);
    }
}
