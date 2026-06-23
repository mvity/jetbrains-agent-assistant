package com.xais.agentassistant.agent.core;

public record AgentPermissionDecision(
        String requestId,
        AgentSessionRef sessionRef,
        String decision
) {
}

