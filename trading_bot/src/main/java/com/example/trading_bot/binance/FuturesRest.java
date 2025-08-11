package com.example.trading_bot.binance;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class FuturesRest {
    private final FuturesHttp http;
    public FuturesRest(FuturesHttp http) { this.http = http; }

    public long serverTime() {
        return http.getPublic("/fapi/v1/time").path("serverTime").asLong();
    }

    public JsonNode exchangeInfo() {
        return http.getPublic("/fapi/v1/exchangeInfo");
    }

    public String startListenKey() {
        return http.postSigned("/fapi/v1/listenKey", Map.of())
                .path("listenKey").asText();
    }

    public void keepAliveListenKey(String listenKey) {
        http.putSigned("/fapi/v1/listenKey", Map.of("listenKey", listenKey));
    }

    public com.fasterxml.jackson.databind.JsonNode klines(String symbol, String interval, int limit) {
        return http.getPublic("/fapi/v1/klines", Map.of(
                "symbol", symbol,
                "interval", interval,
                "limit", String.valueOf(limit)
        ));
    }
}
