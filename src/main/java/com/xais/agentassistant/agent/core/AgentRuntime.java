package com.xais.agentassistant.agent.core;

import java.util.List;

public interface AgentRuntime {
    String id();

    AgentCapabilities capabilities();

    AgentHealth health(AgentRuntimeConfig config);

    AgentSessionRef startSession(AgentStartRequest request);

    void sendMessage(AgentSendRequest request, AgentEventSink sink);

    void abort(AgentSessionRef sessionRef);

    List<AgentSessionSummary> listSessions(AgentProjectRef projectRef);

    List<AgentMessage> loadMessages(AgentSessionRef sessionRef);

    void approvePermission(AgentPermissionDecision decision);

    void dispose(AgentSessionRef sessionRef);
}

