package com.xais.agentassistant.agent.core;

public record AgentSessionRef(
        String runtimeId,
        String providerSessionId,
        String projectRoot,
        String workspaceId
) {
}

