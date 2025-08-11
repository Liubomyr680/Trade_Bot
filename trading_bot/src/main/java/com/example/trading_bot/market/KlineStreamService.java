package com.example.trading_bot.market;

import com.example.trading_bot.config.AppProps;
import com.example.trading_bot.config.BinanceProps;
import com.example.trading_bot.ws.WsClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KlineStreamService {
    private final AppProps app;
    private final BinanceProps cfg;
    private WsClient ws;
    private final Deque<Map<String,Object>> lastBars = new ArrayDeque<>();

    public void start() {
        String stream = app.getSymbol().toLowerCase() + "@kline_" + app.getInterval();
        String url = cfg.getWsStreamBase() + "/ws/" + stream;
        ws = new WsClient(url, this::onMessage);
        log.info("Subscribed to {}", stream);
    }

    private void onMessage(JsonNode root) {
        JsonNode k = root.path("k");
        if (!k.isMissingNode() && k.path("x").asBoolean(false)) {
            var bar = Map.<String,Object>of(
                    "t", k.path("t").asLong(),
                    "o", k.path("o").asText(),
                    "h", k.path("h").asText(),
                    "l", k.path("l").asText(),
                    "c", k.path("c").asText(),
                    "v", k.path("v").asText()
            );
            lastBars.addLast(bar);
            while (lastBars.size() > app.getBarsBuffer()) lastBars.removeFirst();
            log.info("KLINE {} {} C={}", app.getSymbol(), app.getInterval(), bar.get("c"));
        }
    }

    public Deque<Map<String,Object>> recent() { return lastBars; }

    @PreDestroy
    public void stop() { if (ws != null) ws.close(); }
}
