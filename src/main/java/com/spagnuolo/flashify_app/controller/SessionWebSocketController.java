package com.spagnuolo.flashify_app.controller;

import com.spagnuolo.flashify_app.entity.Session;
import com.spagnuolo.flashify_app.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class SessionWebSocketController {

    private final SessionService sessionService;

    // Active player reveals the word to both players
    @MessageMapping("/session/{sessionId}/reveal")
    @SendTo("/topic/session/{sessionId}")
    public Session revealWord(
            @DestinationVariable String sessionId,
            RevealMessage message
    ) {
        return sessionService.revealWord(
                UUID.fromString(sessionId),
                message.revealedBy()
        );
    }

    // Active player reveals the hint to both players
    @MessageMapping("/session/{sessionId}/hint")
    @SendTo("/topic/session/{sessionId}")
    public Session revealHint(@DestinationVariable String sessionId) {
        return sessionService.revealHint(UUID.fromString(sessionId));
    }

    // Active player moves to the next word
    @MessageMapping("/session/{sessionId}/next")
    @SendTo("/topic/session/{sessionId}")
    public Session nextWord(
            @DestinationVariable String sessionId,
            NextWordMessage message
    ) {
        return sessionService.nextWord(
                UUID.fromString(sessionId),
                message.currentTurn()
        );
    }

    // Notify both participants when session becomes active
    // Frontend sends to: /app/session/{sessionId}/join
    // Everyone subscribed to: /topic/session/{sessionId} receives the update
    @MessageMapping("/session/{sessionId}/join")
    @SendTo("/topic/session/{sessionId}")
    public Session joinSession(@DestinationVariable String sessionId) {
        return sessionService.findById(UUID.fromString(sessionId))
                .orElseThrow(() -> new RuntimeException("Session not found"));
    }

    public record RevealMessage(String revealedBy) {}
    public record NextWordMessage(String currentTurn) {}
}