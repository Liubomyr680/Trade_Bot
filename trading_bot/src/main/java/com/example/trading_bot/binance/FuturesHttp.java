package com.example.trading_bot.binance;

import com.example.trading_bot.config.BinanceProps;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class FuturesHttp {
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper om = new ObjectMapper();

    private final String apiKey;
    private final String secret;
    private final BinanceProps props;

    public FuturesHttp(@Value("${BINANCE_API_KEY}") String apiKey,
                       @Value("${BINANCE_API_SECRET}") String secret,
                       BinanceProps props) {
        this.apiKey = apiKey;
        this.secret = secret;
        this.props = props;
    }

    private HttpRequest.Builder base(String pathWithQuery) {
        return HttpRequest.newBuilder()
                .uri(URI.create(props.getRestBase() + pathWithQuery))
                .timeout(Duration.ofSeconds(10))
                .header("X-MBX-APIKEY", apiKey);
    }

    public JsonNode getPublic(String path) {
        try {
            var req = base(path).GET().build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return om.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("GET public " + path, e);
        }
    }

    public JsonNode getPublic(String path, Map<String, String> params) {
        String query = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        return getPublic(path + "?" + query);
    }

    public JsonNode getSigned(String path, Map<String,String> params) {
        try {
            params = new LinkedHashMap<>(params);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("recvWindow", String.valueOf(props.getRecvWindowMs()));
            String qs = Signer.queryString(params);
            String sign = Signer.hmacSha256(secret, qs);
            var req = base(path + "?" + qs + "&signature=" + sign).GET().build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return om.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("GET signed " + path, e);
        }
    }

    public JsonNode postSigned(String path, Map<String,String> params) {
        try {
            params = new LinkedHashMap<>(params);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("recvWindow", String.valueOf(props.getRecvWindowMs()));
            String qs = Signer.queryString(params);
            String sign = Signer.hmacSha256(secret, qs);
            var req = base(path)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(qs + "&signature=" + sign))
                    .build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            return om.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("POST signed " + path, e);
        }
    }

    public void putSigned(String path, Map<String,String> params) {
        try {
            params = new LinkedHashMap<>(params);
            params.put("timestamp", String.valueOf(System.currentTimeMillis()));
            params.put("recvWindow", String.valueOf(props.getRecvWindowMs()));
            String qs = Signer.queryString(params);
            String sign = Signer.hmacSha256(secret, qs);
            var req = base(path + "?" + qs + "&signature=" + sign)
                    .PUT(HttpRequest.BodyPublishers.noBody()).build();
            var res = http.send(req, HttpResponse.BodyHandlers.ofString());
            om.readTree(res.body());
        } catch (Exception e) {
            throw new RuntimeException("PUT signed " + path, e);
        }
    }
}
