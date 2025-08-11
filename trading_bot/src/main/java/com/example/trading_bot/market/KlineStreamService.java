package com.example.trading_bot.market;

import com.example.trading_bot.config.AppProps;
import com.example.trading_bot.config.BinanceProps;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class KlineStreamService {
    private final AppProps app;
    private final BinanceProps cfg;
    private final CandleBufferService buffer;

    private com.example.trading_bot.ws.WsClient ws;

    public void start() {
        String stream = app.getSymbol().toLowerCase() + "@kline_" + app.getInterval();
        String url = cfg.getWsStreamBase() + "/ws/" + stream;
        ws = new com.example.trading_bot.ws.WsClient(url, this::onMessage);
        log.info("Subscribed to {}", stream);
    }

    private void onMessage(com.fasterxml.jackson.databind.JsonNode root) {
        var k = root.path("k");
        if (!k.isMissingNode() && k.path("x").asBoolean(false)) { // closed
            Candle c = new Candle(
                    k.path("t").asLong(),
                    k.path("o").asDouble(),
                    k.path("h").asDouble(),
                    k.path("l").asDouble(),
                    k.path("c").asDouble(),
                    k.path("v").asDouble()
            );
            buffer.push(c);
            log.info("KLINE {} {} C={} | buffer={}", app.getSymbol(), app.getInterval(), c.close(), buffer.size());
            // тут далі будемо викликати індикатори/GPT
        }
    }

    @PreDestroy
    public void stop() { if (ws != null) ws.close(); }
}
