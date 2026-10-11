package com.syncboard.syncboard_backend;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Room {
    private static final int MAX_DRAWING_EVENTS = 3_000;
    private static final int MAX_CHAT_MESSAGES = 500;

    private final String code;
    private final String createdBy;
    private final Set<String> participants = ConcurrentHashMap.newKeySet();
    private final Deque<Map<String, Object>> drawingHistory = new ArrayDeque<>();
    private final Deque<Map<String, Object>> chatHistory = new ArrayDeque<>();
    private final Deque<Map<String, Object>> redoHistory = new ArrayDeque<>();

    public Room(String createdBy) {
        this.code = UUID.randomUUID()
                .toString()
                .substring(0, 8)
                .toUpperCase();
        this.createdBy = createdBy.trim();
        this.participants.add(this.createdBy);
    }

    public String getCode() {
        return code;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Set<String> getParticipants() {
        return Set.copyOf(participants);
    }

    public void addParticipant(String name) {
        if (name != null && !name.isBlank()) {
            participants.add(name.trim());
        }
    }

    public synchronized List<Map<String, Object>> getDrawingHistory() {
        return List.copyOf(drawingHistory);
    }

    public synchronized void addDrawingEvent(Map<String, Object> event) {
        if (event == null) return;
        redoHistory.clear();

        if (drawingHistory.size() >= MAX_DRAWING_EVENTS) {
            drawingHistory.removeFirst();
        }

        drawingHistory.addLast(immutableCopy(event));
    }

  public synchronized void clearDrawingHistory() {
    drawingHistory.clear();
    redoHistory.clear();
}

public synchronized Map<String, Object> removeLastDrawingEvent() {
    if (drawingHistory.isEmpty()) return null;

    Map<String, Object> removed = drawingHistory.removeLast();
    redoHistory.addLast(removed);
    return removed;
}
public synchronized Map<String, Object> redoLastDrawingEvent() {
    if (redoHistory.isEmpty()) return null;

    Map<String, Object> restored = redoHistory.removeLast();

    if (drawingHistory.size() >= MAX_DRAWING_EVENTS) {
        drawingHistory.removeFirst();
    }

    drawingHistory.addLast(restored);
    return restored;
}
    public synchronized List<Map<String, Object>> getChatHistory() {
        return List.copyOf(chatHistory);
    }

    public synchronized void addChatMessage(Map<String, Object> message) {
        if (message == null) return;

        if (chatHistory.size() >= MAX_CHAT_MESSAGES) {
            chatHistory.removeFirst();
        }

        chatHistory.addLast(immutableCopy(message));
    }

    private Map<String, Object> immutableCopy(Map<String, Object> source) {
        return Collections.unmodifiableMap(new HashMap<>(source));
    }
}