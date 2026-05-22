package com.spagnuolo.flashify_app.config;

import com.spagnuolo.flashify_app.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private final SessionService sessionService;

    // run on startup and every hour
    @Scheduled(fixedRate = 3600000, initialDelay = 0)
    public void cleanUpStaleSessions() {
        sessionService.cleanUpStaleSessions();
    }
}