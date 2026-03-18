package com.ddd4j.cloud.web.utils;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class BaseWebSocketServer {

    @OnOpen
    public void onOpen(Session session) {
        log.info("WebSocket connection opened: {}", session.getId());
    }

    @OnClose
    public void onClose(Session session) {
        log.info("WebSocket connection closed: {}", session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("Received message: {}", message);
    }

    @OnError
    public void onError(Throwable error) {
        log.error(error.getMessage());
    }
}