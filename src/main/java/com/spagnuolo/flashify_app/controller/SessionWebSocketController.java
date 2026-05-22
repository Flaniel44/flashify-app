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

    // Teacher or student sends a reveal action
    // Frontend sends to: /app/session/{sessionId}/reveal
    // Everyone subscribed to: /topic/session/{sessionId} receives the update
    @MessageMapping("/session/{sessionId}/reveal")
    @SendTo("/topic/session/{sessionId}")
    public Session revealWord(
            @DestinationVariable String sessionId,
            RevealMessage message
    ) {
        return sessionService.revealWord(
                UUID.fromString(sessionId),
                message.revealedBy(),
                message.hintUsed()
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

    public record RevealMessage(String revealedBy, boolean hintUsed) {}
}