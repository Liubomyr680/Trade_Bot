package com.example.trading_bot.market;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

@Slf4j
public class TfCandleBuffer {
    private final String symbol;
    private final String interval;
    private final int capacity;
    private final Deque<Candle> buf = new ArrayDeque<>();

    public TfCandleBuffer(String symbol, String interval, int capacity) {
        this.symbol = symbol;
        this.interval = interval;
        this.capacity = capacity;
    }

    /** ініціалізація з REST /klines масиву */
    public void initFromKlines(JsonNode arr) {
        buf.clear();
        for (JsonNode k : arr) {
            push(new Candle(
                    k.get(0).asLong(),
                    k.get(1).asDouble(),
                    k.get(2).asDouble(),
                    k.get(3).asDouble(),
                    k.get(4).asDouble(),
                    k.get(5).asDouble()
            ));
        }
        log.info("Candle buffer initialized: {} bars ({} {})", buf.size(), symbol, interval);
    }

    /** додаємо закриту свічку з WS */
    public void push(Candle c) {
        Candle last = buf.peekLast();
        if (last != null && last.openTimeMs() == c.openTimeMs()) {
            buf.removeLast(); // уникнути дубля останнього бару
        }
        buf.addLast(c);
        while (buf.size() > capacity) buf.removeFirst();
    }

    public List<Candle> snapshot() { return List.copyOf(buf); }
    public int size() { return buf.size(); }
    public String interval() { return interval; }
    public String symbol() { return symbol; }
}
