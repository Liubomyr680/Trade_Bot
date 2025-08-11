package com.example.trading_bot.indicators;

import com.example.trading_bot.market.Candle;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeriesBuilder;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class Ta4jSeriesFactory {

    public static Duration parseInterval(String tf) {
        char u = Character.toLowerCase(tf.charAt(tf.length() - 1));
        long v = Long.parseLong(tf.substring(0, tf.length() - 1));
        return switch (u) {
            case 'm' -> Duration.ofMinutes(v);
            case 'h' -> Duration.ofHours(v);
            case 'd' -> Duration.ofDays(v);
            default  -> Duration.ofMinutes(1);
        };
    }

    public static BarSeries build(String name, List<Candle> candles, Duration barDuration) {
        BarSeries s = new BaseBarSeriesBuilder()
                .withName(name) // DecimalNum за замовчуванням
                .build();
        for (Candle c : candles) {
            ZonedDateTime endTime = Instant.ofEpochMilli(c.openTimeMs())
                    .plus(barDuration).atZone(ZoneOffset.UTC);

            s.addBar(new BaseBar(
                    barDuration,
                    endTime,
                    num(c.open()),
                    num(c.high()),
                    num(c.low()),
                    num(c.close()),
                    num(c.volume()),
                    num(c.volume()) // amount — ставимо volume
            ));
        }
        return s;
    }

    private static Num num(double v) { return DecimalNum.valueOf(v); }
}