package com.syncboard.syncboard_backend;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

@Service
public class RoomService {
    private static final int MAX_NAME_LENGTH = 40;

    // Rooms and their board/chat history exist in memory only.
    // Restarting the backend clears them; no database is used.
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public Room createRoom(String createdBy) {
        String cleanName = validateName(createdBy);

        Room room;
        do {
            room = new Room(cleanName);
        } while (rooms.putIfAbsent(room.getCode(), room) != null);

        return room;
    }

    public Room findRoom(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }

        return rooms.get(normalizeCode(code));
    }

    public Room joinRoom(String code, String name) {
        String cleanName = validateName(name);
        Room room = findRoom(code);

        if (room == null) {
            return null;
        }

        room.addParticipant(cleanName);
        return room;
    }

    public List<Room> findAllRooms() {
        return List.copyOf(rooms.values());
    }

    public boolean deleteRoom(String code) {
        if (code == null || code.isBlank()) {
            return false;
        }

        return rooms.remove(normalizeCode(code)) != null;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required.");
        }

        String cleanName = name.trim();

        if (cleanName.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(
                    "Name must be 40 characters or fewer."
            );
        }

        return cleanName;
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase();
    }
}