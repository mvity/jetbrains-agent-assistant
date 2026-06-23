package com.xais.agentassistant.agent.core;

public record AgentHealth(boolean healthy, String message) {
    public static AgentHealth healthy(String message) {
        return new AgentHealth(true, message);
    }

    public static AgentHealth unhealthy(String message) {
        return new AgentHealth(false, message);
    }
}

