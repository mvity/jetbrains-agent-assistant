package com.xais.agentassistant.agent.core;

import java.util.Map;

public record AgentRuntimeConfig(Map<String, String> values) {
    public static AgentRuntimeConfig empty() {
        return new AgentRuntimeConfig(Map.of());
    }
}

