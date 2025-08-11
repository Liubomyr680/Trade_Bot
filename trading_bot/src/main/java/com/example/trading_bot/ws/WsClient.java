package com.example.trading_bot.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;

@Slf4j
public class WsClient implements WebSocket.Listener, AutoCloseable {
    private final ObjectMapper om = new ObjectMapper();
    private final Consumer<JsonNode> onJson;
    private WebSocket ws;

    public WsClient(String url, Consumer<JsonNode> onJson) {
        this.onJson = onJson;
        this.ws = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build()
                .newWebSocketBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .buildAsync(URI.create(url), this)
                .join();
    }

    @Override public void onOpen(WebSocket webSocket) {
        WebSocket.Listener.super.onOpen(webSocket);
        log.info("WS open");
    }

    @Override public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
        try { onJson.accept(om.readTree(data.toString())); }
        catch (Exception e) { log.warn("WS parse error: {}", e.getMessage()); }
        webSocket.request(1);
        return null;
    }

    @Override public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        webSocket.request(1); return null;
    }

    @Override public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        webSocket.sendPong(message); webSocket.request(1); return null;
    }

    @Override public void onError(WebSocket webSocket, Throwable error) {
        log.error("WS error", error);
    }

    @Override public void close() { if (ws != null) ws.abort(); }
}