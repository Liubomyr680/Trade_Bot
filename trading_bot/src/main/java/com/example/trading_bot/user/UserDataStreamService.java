package com.example.trading_bot.user;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.config.BinanceProps;
import com.example.trading_bot.ws.WsClient;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserDataStreamService {
    private final FuturesRest rest;
    private final BinanceProps cfg;
    private String listenKey;
    private WsClient ws;

    public void start() {
        listenKey = rest.startListenKey();
        log.info("listenKey={}", listenKey);
        String url = cfg.getWsStreamBase() + "/ws/" + listenKey;
        ws = new WsClient(url, this::onMessage);
    }

    private void onMessage(JsonNode root) {
        String e = root.path("e").asText("");
        switch (e) {
            case "ACCOUNT_UPDATE" -> log.info("ACCOUNT_UPDATE: {}", root);
            case "ORDER_TRADE_UPDATE" -> log.info("ORDER_TRADE_UPDATE: {}", root);
            case "MARGIN_CALL" -> log.warn("MARGIN_CALL: {}", root);
            default -> log.debug("USERDATA: {}", root);
        }
    }

    @Scheduled(fixedDelay = 30*60*1000L, initialDelay = 30*60*1000L)
    public void keepAlive() {
        if (listenKey != null) {
            rest.keepAliveListenKey(listenKey);
            log.info("listenKey keepAlive OK");
        }
    }

    @PreDestroy
    public void stop() { if (ws != null) ws.close(); }
}