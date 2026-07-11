package com.rc.readcompass.notification.service;

import java.io.IOException;
import java.net.InetAddress;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSseService {

  private final Map<UUID, SseEmitter> emitters = new ConcurrentHashMap<>();
  private static final long SSE_TIMEOUT = Duration.ofMinutes(60).toMillis();

  public SseEmitter subscribe(UUID userId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);
    emitters.put(userId, emitter);

    try {
      log.info(
          "[SSE CONNECT] host={}, userId={}, emitterSize={}",
          InetAddress.getLocalHost().getHostName(),
          userId,
          emitters.size()
      );
    } catch (IOException e) {
      log.warn("Failed to get host name.", e);
    }

    emitter.onCompletion(() -> {
      emitters.remove(userId);
      log.info("[SSE COMPLETE] userId={}, emitterSize={}", userId, emitters.size());
    });

    emitter.onTimeout(() -> {
      emitters.remove(userId);
      log.info("[SSE TIMEOUT] userId={}, emitterSize={}", userId, emitters.size());
    });

    emitter.onError(e -> {
      emitters.remove(userId);
      log.warn("[SSE ERROR] userId={}", userId, e);
    });

    try {
      emitter.send(
          SseEmitter.event()
              .name("connect")
              .data("connected")
      );
    } catch (IOException e) {
      emitters.remove(userId);
      log.error("[SSE CONNECT FAIL] userId={}", userId, e);
    }

    return emitter;
  }

  public void send(UUID receiverId) {

    try {
      log.info(
          "[SSE SEND] host={}, receiverId={}, emitterSize={}",
          InetAddress.getLocalHost().getHostName(),
          receiverId,
          emitters.size()
      );
    } catch (IOException e) {
      log.warn("Failed to get host name.", e);
    }

    SseEmitter emitter = emitters.get(receiverId);

    if (emitter == null) {
      log.warn("[SSE SEND FAIL] receiverId={}, emitter not found", receiverId);
      return;
    }

    try {
      emitter.send(
          SseEmitter.event()
              .name("notification")
              .data("new")
      );

      log.info("[SSE SEND SUCCESS] receiverId={}", receiverId);

    } catch (IOException e) {
      emitters.remove(receiverId);
      log.error("[SSE SEND ERROR] receiverId={}", receiverId, e);
    }
  }
}