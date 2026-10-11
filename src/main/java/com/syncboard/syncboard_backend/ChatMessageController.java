package com.syncboard.syncboard_backend;

import java.time.Instant;
import java.util.Map;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatMessageController {
    private static final int MAX_NAME_LENGTH = 40;
    private static final int MAX_MESSAGE_LENGTH = 2_000;

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public ChatMessageController(
            SimpMessagingTemplate messagingTemplate,
            RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    @MessageMapping("/rooms/{roomCode}/chat")
    public void relayChat(
            @DestinationVariable String roomCode,
            Map<String, Object> incomingMessage) {
        Room room = roomService.findRoom(roomCode);

        if (room == null || incomingMessage == null) {
            return;
        }

        Object senderValue = incomingMessage.get("sender");
        Object textValue = incomingMessage.get("text");

        if (!(senderValue instanceof String sender)
                || !(textValue instanceof String text)) {
            return;
        }

        sender = sender.trim();
        text = text.trim();

        if (sender.isEmpty() || sender.length() > MAX_NAME_LENGTH
                || text.isEmpty() || text.length() > MAX_MESSAGE_LENGTH) {
            return;
        }

        Map<String, Object> message = Map.of(
                "sender", sender,
                "text", text,
                "timestamp", Instant.now().toString()
        );

        room.addChatMessage(message);

        messagingTemplate.convertAndSend(
                "/topic/rooms/" + room.getCode() + "/chat",
                (Object) message
        );
    }
}