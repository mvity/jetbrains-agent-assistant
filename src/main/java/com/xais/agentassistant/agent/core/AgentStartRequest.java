package com.xais.agentassistant.agent.core;

public record AgentStartRequest(
        String runtimeId,
        String projectRoot,
        String workspaceId,
        String title
) {
}

