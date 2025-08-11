package com.example.trading_bot.indicators;

import com.example.trading_bot.market.Candle;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.ATRIndicator;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.MACDIndicator;
import org.ta4j.core.indicators.RSIIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndicatorsService {

    public FeatureFrame compute(List<Candle> candles, String interval) {
        if (candles == null || candles.size() < 60) return null;

        Duration dur = Ta4jSeriesFactory.parseInterval(interval);
        BarSeries s = Ta4jSeriesFactory.build("series-"+interval, candles, dur);

        var close = new ClosePriceIndicator(s);
        var ema20 = new EMAIndicator(close, 20);
        var ema50 = new EMAIndicator(close, 50);
        var rsi14 = new RSIIndicator(close, 14);
        var macd  = new MACDIndicator(close, 12, 26);
        var macdSig = new EMAIndicator(macd, 9);
        var atr14 = new ATRIndicator(s, 14);

        int i = s.getEndIndex();
        double c    = close.getValue(i).doubleValue();
        double e20  = ema20.getValue(i).doubleValue();
        double e50  = ema50.getValue(i).doubleValue();
        double rsi  = rsi14.getValue(i).doubleValue();
        double macdV= macd.getValue(i).doubleValue();
        double macdS= macdSig.getValue(i).doubleValue();
        double macdH= macdV - macdS;
        double atrP = (atr14.getValue(i).doubleValue() / c) * 100.0;

        return new FeatureFrame(c, e20, e50, rsi, macdV, macdS, macdH, atrP);
    }
}
