package com.xais.agentassistant.agent.core;

public record AgentCapabilities(
        boolean supportsStreaming,
        boolean supportsImages,
        boolean supportsHistory,
        boolean supportsRevert,
        boolean supportsPermissions,
        boolean supportsMcp,
        boolean supportsSubagents,
        boolean supportsPlanMode,
        boolean supportsStructuredOutput
) {
    public static AgentCapabilities minimalStreaming() {
        return new AgentCapabilities(true, false, false, false, false, false, false, false, false);
    }
}

