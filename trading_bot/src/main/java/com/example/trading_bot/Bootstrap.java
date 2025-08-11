package com.example.trading_bot;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.market.KlineStreamService;
import com.example.trading_bot.user.UserDataStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class Bootstrap implements CommandLineRunner {
    private final FuturesRest rest;
    private final KlineStreamService klines;
    private final UserDataStreamService userData;

    @Override public void run(String... args) {
        long serverTime = rest.serverTime();
        log.info("Binance serverTime: {}", serverTime);
        int symbols = rest.exchangeInfo().path("symbols").size();
        log.info("exchangeInfo symbols: {}", symbols);

        klines.start();     // BTCUSDT@kline_1m
        userData.start();   // user-data stream
    }
}
