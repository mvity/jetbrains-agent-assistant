package com.xais.agentassistant.agent.service;

import com.xais.agentassistant.agent.core.AgentEvent;
import com.xais.agentassistant.agent.core.AgentEventSink;
import com.xais.agentassistant.agent.core.AgentMessage;
import com.xais.agentassistant.agent.core.AgentMessagePart;
import com.xais.agentassistant.agent.core.AgentRuntime;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.core.AgentSessionRef;
import com.xais.agentassistant.agent.core.AgentStartRequest;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class AgentSessionService {
    private final AgentRuntimeRegistry registry;
    private final String defaultRuntimeId;
    private final ConcurrentMap<String, AgentSessionRef> currentSessions = new ConcurrentHashMap<>();

    public AgentSessionService(AgentRuntimeRegistry registry, String defaultRuntimeId) {
        this.registry = registry;
        this.defaultRuntimeId = defaultRuntimeId;
    }

    public AgentSessionRef ensureSession(String projectRoot, AgentEventSink sink) {
        return ensureSession(defaultRuntimeId, projectRoot, sink);
    }

    public AgentSessionRef ensureSession(String runtimeId, String projectRoot, AgentEventSink sink) {
        AgentSessionRef existing = currentSessions.get(runtimeId);
        if (existing != null) {
            return existing;
        }

        AgentRuntime runtime = registry.require(runtimeId);
        AgentSessionRef created = runtime.startSession(new AgentStartRequest(
                runtimeId,
                projectRoot,
                projectRoot,
                "New session"
        ));
        currentSessions.put(runtimeId, created);
        sink.emit(AgentEvent.sessionCreated(created));
        return created;
    }

    public void sendText(String projectRoot, String text, AgentEventSink sink) {
        sendText(defaultRuntimeId, projectRoot, text, sink);
    }

    public void sendText(String runtimeId, String projectRoot, String text, AgentEventSink sink) {
        AgentSessionRef sessionRef = ensureSession(runtimeId, projectRoot, sink);
        AgentRuntime runtime = registry.require(sessionRef.runtimeId());
        AgentMessage message = new AgentMessage(
                UUID.randomUUID().toString(),
                "user",
                List.of(AgentMessagePart.text(text)),
                Instant.now()
        );
        runtime.sendMessage(new AgentSendRequest(sessionRef, message), sink);
    }
}
