package com.syncboard.syncboard_backend;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    public ResponseEntity<Room> createRoom(
            @RequestBody(required = false) Map<String, ?> request) {
        String createdBy = requiredText(request, "createdBy");
        Room room = roomService.createRoom(createdBy);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    @PostMapping("/{code}/join")
    public ResponseEntity<Room> joinRoom(
            @PathVariable String code,
            @RequestBody(required = false) Map<String, ?> request) {
        String name = requiredText(request, "name");
        Room room = roomService.joinRoom(code, name);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(room);
    }

    @GetMapping("/{code}")
    public ResponseEntity<Room> getRoom(@PathVariable String code) {
        Room room = roomService.findRoom(code);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(room);
    }

    @GetMapping("/{code}/drawing")
    public ResponseEntity<List<Map<String, Object>>> getDrawingHistory(
            @PathVariable String code) {
        Room room = roomService.findRoom(code);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(room.getDrawingHistory());
    }

    @GetMapping("/{code}/chat")
    public ResponseEntity<List<Map<String, Object>>> getChatHistory(
            @PathVariable String code) {
        Room room = roomService.findRoom(code);

        if (room == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(room.getChatHistory());
    }

    @DeleteMapping("/{code}")
    public ResponseEntity<Void> deleteRoom(@PathVariable String code) {
        if (!roomService.deleteRoom(code)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    private String requiredText(Map<String, ?> request, String fieldName) {
        if (request == null || !(request.get(fieldName) instanceof String value)
                || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }

        return value.trim();
    }
}