package com.rc.readcompass.notification.service;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
public class NotificationSseService {

  private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
  private static final long SSE_TIMEOUT = Duration.ofMinutes(60).toMillis();

  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
    emitters.put(userId, emitter);
    emitter.onCompletion(() -> emitters.remove(userId));
    emitter.onTimeout(() -> emitters.remove(userId));
    emitter.onError(e -> emitters.remove(userId));

    try {
      emitter.send(
          SseEmitter.event()
              .name("connect")
              .data("connected")
      );
    } catch (IOException e) {
      emitters.remove(userId);
    }

    return emitter;
  }

  public void send(UUID receiverId) {
    SseEmitter emitter = emitters.get(receiverId);
    if (emitter == null) {
      return;
    }

    try {
      emitter.send(
          SseEmitter.event()
              .name("notification")
              .data("new")
      );
    } catch (IOException e) {
      emitters.remove(receiverId);
    }

  }
}
