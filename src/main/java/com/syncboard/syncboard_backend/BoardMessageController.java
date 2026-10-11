package com.syncboard.syncboard_backend;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class BoardMessageController {
    private static final Set<String> SHAPES = Set.of("rectangle", "circle");
    private static final Set<String> TOOLS = Set.of("pen", "eraser");

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public BoardMessageController(
            SimpMessagingTemplate messagingTemplate,
            RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    @MessageMapping("/rooms/{roomCode}/drawing")
    public void relayDrawing(
            @DestinationVariable String roomCode,
            Map<String, Object> drawingMessage) {
        Room room = roomService.findRoom(roomCode);

        if (room == null || drawingMessage == null) {
            return;
        }

        Object typeValue = drawingMessage.get("type");
        String type = typeValue instanceof String value ? value : "stroke";

if ("clear".equals(type)) {
    room.clearDrawingHistory();
} else if ("undo".equals(type)) {
    Map<String, Object> removedEvent = room.removeLastDrawingEvent();
    if (removedEvent == null || !(removedEvent.get("eventId") instanceof String eventId)) {
        return;
    }
    drawingMessage = Map.of("type", "undo", "eventId", eventId);
} else if ("redo".equals(type)) {
    Map<String, Object> restoredEvent = room.redoLastDrawingEvent();
    if (restoredEvent == null) return;
    drawingMessage = Map.of("type", "redo", "drawingEvent", restoredEvent);
} else if ("stroke".equals(type) && isValidStroke(drawingMessage)) {
    room.addDrawingEvent(drawingMessage);
} else if ("shape".equals(type) && isValidShape(drawingMessage)) {
    room.addDrawingEvent(drawingMessage);
} else if ("note".equals(type) && isValidNote(drawingMessage)) {
    room.addDrawingEvent(drawingMessage);
} else {
    return;
}
        messagingTemplate.convertAndSend(
                "/topic/rooms/" + room.getCode() + "/drawing",
                (Object) drawingMessage
        );
    }

    private boolean isValidStroke(Map<String, Object> message) {
        Object pointsValue = message.get("points");
        Object widthValue = message.get("width");
        Object toolValue = message.get("tool");

        if (!(pointsValue instanceof List<?> points)
                || points.isEmpty()
                || points.size() > 10_000
                || !isNumber(widthValue)
                || ((Number) widthValue).doubleValue() <= 0
                || ((Number) widthValue).doubleValue() > 100) {
            return false;
        }

        if (toolValue != null
                && (!(toolValue instanceof String tool) || !TOOLS.contains(tool))) {
            return false;
        }

        for (Object pointValue : points) {
            if (!(pointValue instanceof Map<?, ?> point)
                    || !isNumber(point.get("x"))
                    || !isNumber(point.get("y"))) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidShape(Map<String, Object> message) {
        Object shapeValue = message.get("shape");
        Object widthValue = message.get("width");

        return shapeValue instanceof String shape
                && SHAPES.contains(shape)
                && isNumber(widthValue)
                && ((Number) widthValue).doubleValue() > 0
                && ((Number) widthValue).doubleValue() <= 100
                && isNumber(message.get("x"))
                && isNumber(message.get("y"))
                && isNumber(message.get("w"))
                && isNumber(message.get("h"));
    }

    private boolean isValidNote(Map<String, Object> message) {
        Object textValue = message.get("text");

        return textValue instanceof String text
                && !text.isBlank()
                && text.length() <= 500
                && isNumber(message.get("x"))
                && isNumber(message.get("y"));
    }

    private boolean isNumber(Object value) {
        return value instanceof Number number
                && Double.isFinite(number.doubleValue());
    }
}