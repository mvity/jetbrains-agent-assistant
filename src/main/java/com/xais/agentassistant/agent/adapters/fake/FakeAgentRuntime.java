package com.xais.agentassistant.agent.adapters.fake;

import com.xais.agentassistant.agent.core.AgentCapabilities;
import com.xais.agentassistant.agent.core.AgentEvent;
import com.xais.agentassistant.agent.core.AgentEventSink;
import com.xais.agentassistant.agent.core.AgentHealth;
import com.xais.agentassistant.agent.core.AgentMessage;
import com.xais.agentassistant.agent.core.AgentMessagePart;
import com.xais.agentassistant.agent.core.AgentPermissionDecision;
import com.xais.agentassistant.agent.core.AgentProjectRef;
import com.xais.agentassistant.agent.core.AgentRuntime;
import com.xais.agentassistant.agent.core.AgentRuntimeConfig;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.core.AgentSessionRef;
import com.xais.agentassistant.agent.core.AgentSessionSummary;
import com.xais.agentassistant.agent.core.AgentStartRequest;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class FakeAgentRuntime implements AgentRuntime {
    public static final String ID = "fake-agent";

    private final ConcurrentMap<String, List<AgentMessage>> messagesBySession = new ConcurrentHashMap<>();

    @Override
    public String id() {
        return ID;
    }

    @Override
    public AgentCapabilities capabilities() {
        return AgentCapabilities.minimalStreaming();
    }

    @Override
    public AgentHealth health(AgentRuntimeConfig config) {
        return AgentHealth.healthy("Fake agent is ready");
    }

    @Override
    public AgentSessionRef startSession(AgentStartRequest request) {
        AgentSessionRef sessionRef = new AgentSessionRef(
                id(),
                UUID.randomUUID().toString(),
                request.projectRoot(),
                request.workspaceId()
        );
        messagesBySession.put(sessionRef.providerSessionId(), new ArrayList<>());
        return sessionRef;
    }

    @Override
    public void sendMessage(AgentSendRequest request, AgentEventSink sink) {
        List<AgentMessage> messages = messagesBySession.computeIfAbsent(
                request.sessionRef().providerSessionId(),
                ignored -> new ArrayList<>()
        );
        messages.add(request.message());

        String userText = request.message().parts().stream()
                .filter(part -> "text".equals(part.type()))
                .map(AgentMessagePart::text)
                .findFirst()
                .orElse("");

        AgentMessage assistantMessage = new AgentMessage(
                UUID.randomUUID().toString(),
                "assistant",
                List.of(AgentMessagePart.text("Fake agent received: " + userText)),
                Instant.now()
        );
        messages.add(assistantMessage);

        sink.emit(AgentEvent.messageDelta(request.sessionRef(), "Fake agent received: " + userText));
        sink.emit(AgentEvent.done(request.sessionRef()));
    }

    @Override
    public void abort(AgentSessionRef sessionRef) {
        // Fake runtime is synchronous; there is nothing to abort.
    }

    @Override
    public List<AgentSessionSummary> listSessions(AgentProjectRef projectRef) {
        return messagesBySession.keySet().stream()
                .map(id -> new AgentSessionRef(ID, id, projectRef.projectRoot(), projectRef.projectRoot()))
                .map(ref -> new AgentSessionSummary(ref, "Fake session", Instant.now(), Instant.now()))
                .toList();
    }

    @Override
    public List<AgentMessage> loadMessages(AgentSessionRef sessionRef) {
        return List.copyOf(messagesBySession.getOrDefault(sessionRef.providerSessionId(), List.of()));
    }

    @Override
    public void approvePermission(AgentPermissionDecision decision) {
        // Fake runtime does not request permissions.
    }

    @Override
    public void dispose(AgentSessionRef sessionRef) {
        messagesBySession.remove(sessionRef.providerSessionId());
    }
}

