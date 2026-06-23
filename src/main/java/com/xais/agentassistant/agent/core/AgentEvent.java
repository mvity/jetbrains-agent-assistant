package com.xais.agentassistant.agent.core;

import java.time.Instant;
import java.util.Objects;

public final class AgentEvent {
    private final String type;
    private final AgentSessionRef sessionRef;
    private final String text;
    private final String message;
    private final Instant createdAt;

    private AgentEvent(String type, AgentSessionRef sessionRef, String text, String message) {
        this.type = Objects.requireNonNull(type, "type");
        this.sessionRef = sessionRef;
        this.text = text;
        this.message = message;
        this.createdAt = Instant.now();
    }

    public static AgentEvent sessionCreated(AgentSessionRef sessionRef) {
        return new AgentEvent("agent.session.created", sessionRef, null, null);
    }

    public static AgentEvent messageDelta(AgentSessionRef sessionRef, String text) {
        return new AgentEvent("agent.message.delta", sessionRef, text, null);
    }

    public static AgentEvent done(AgentSessionRef sessionRef) {
        return new AgentEvent("agent.done", sessionRef, null, null);
    }

    public static AgentEvent error(AgentSessionRef sessionRef, String message) {
        return new AgentEvent("agent.error", sessionRef, null, message);
    }

    public String type() {
        return type;
    }

    public AgentSessionRef sessionRef() {
        return sessionRef;
    }

    public String text() {
        return text;
    }

    public String message() {
        return message;
    }

    public Instant createdAt() {
        return createdAt;
    }
}

