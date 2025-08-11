package com.example.trading_bot;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.config.AppProps;
import com.example.trading_bot.indicators.FeatureFrame;
import com.example.trading_bot.indicators.IndicatorsService;
import com.example.trading_bot.market.KlineStreamService;
import com.example.trading_bot.market.MultiTfBuffers;
import com.example.trading_bot.user.UserDataStreamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class Bootstrap implements CommandLineRunner {
    private final FuturesRest rest;
    private final AppProps app;
    private final MultiTfBuffers buffers;
    private final KlineStreamService klineService;
    private final UserDataStreamService userData;
    private final IndicatorsService indicators;

    @Override public void run(String... args) {
        log.info("Binance serverTime: {}", rest.serverTime());
        log.info("exchangeInfo symbols: {}", rest.exchangeInfo().path("symbols").size());

        // 1) Історія по всіх TF
        buffers.initAll();

        // 2) ІНІЦІАЛЬНИЙ АНАЛІЗ по кожному TF (видно в логах одразу)
        for (String tf : app.getIntervals()) {
            var snap = buffers.buffer(tf).snapshot();
            var f = indicators.compute(snap, tf);
            if (f != null) {
                logInd(tf, f);
            } else {
                log.info("IND {} {} | not enough candles yet (have {})", app.getSymbol(), tf, snap.size());
            }
        }

        // 3) Запуск WS (ринок + user-data)
        klineService.startAll();
        userData.start();
    }

    private void logInd(String tf, FeatureFrame f) {
        log.info("IND {} {} | C={} EMA20={} EMA50={} RSI14={} MACD={} Sig={} Hist={} ATR%={}",
                app.getSymbol(), tf,
                r2(f.close()), r2(f.ema20()), r2(f.ema50()),
                r2(f.rsi14()), r3(f.macd()), r3(f.macdSignal()),
                r3(f.macdHist()), r3(f.atrPct()));
    }
    private double r2(double v){ return Math.round(v*100.0)/100.0; }
    private double r3(double v){ return Math.round(v*1000.0)/1000.0; }
}
