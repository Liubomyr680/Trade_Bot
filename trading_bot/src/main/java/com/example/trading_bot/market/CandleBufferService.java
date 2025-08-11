package com.example.trading_bot.market;

import com.example.trading_bot.binance.FuturesRest;
import com.example.trading_bot.config.AppProps;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleBufferService {
    private final FuturesRest rest;
    private final AppProps app;

    private final Deque<Candle> buf = new ArrayDeque<>();

    /** Викликаємо при старті: тягнемо останні N свічок і заповнюємо буфер */
    public void initFromHistory(int n) {
        JsonNode arr = rest.klines(app.getSymbol(), String.valueOf(app.getIntervals()), n);
        List<Candle> list = new ArrayList<>(arr.size());
        for (JsonNode k : arr) {
            // формат масиву klines:
            // [0] openTime, [1] open, [2] high, [3] low, [4] close, [5] volume, ...
            Candle c = new Candle(
                    k.get(0).asLong(),
                    k.get(1).asDouble(),
                    k.get(2).asDouble(),
                    k.get(3).asDouble(),
                    k.get(4).asDouble(),
                    k.get(5).asDouble()
            );
            list.add(c);
        }
        buf.clear();
        for (Candle c : list) push(c);
        log.info("Candle buffer initialized: {} bars ({} {})", buf.size(), app.getSymbol(), app.getIntervals());
    }

    /** Додаємо ЗАКРИТУ свічку з WS; тримаємо не більше app.barsBuffer */
    public void push(Candle c) {
        buf.addLast(c);
        while (buf.size() > app.getBarsBuffer()) buf.removeFirst();
    }

    public List<Candle> snapshot() { return List.copyOf(buf); }
    public int size() { return buf.size(); }
    public Candle last() { return buf.peekLast(); }
}
