package com.example.trading_bot;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.market.CandleBufferService;
import com.example.trading_bot.market.KlineStreamService;
import com.example.trading_bot.user.UserDataStreamService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class Bootstrap implements CommandLineRunner {
    private final FuturesRest rest;
    private final CandleBufferService buffer;
    private final KlineStreamService klines;
    private final UserDataStreamService userData;

    @Override public void run(String... args) {
        long serverTime = rest.serverTime();
        log.info("Binance serverTime: {}", serverTime);
        int symbols = rest.exchangeInfo().path("symbols").size();
        log.info("exchangeInfo symbols: {}", symbols);

        // 1) Історія 100 барів
        buffer.initFromHistory(100);

        JsonNode candles = rest.klines("BTCUSDT", "1m", 100);
        log.info("Loaded {} candles", candles.size());
        log.info("First candle: {}", candles.get(0));

        // 2) Запускаємо WS
        klines.start();
        userData.start();
    }
}
