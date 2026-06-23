package com.xais.agentassistant.agent.core;

public record AgentMessagePart(String type, String text) {
    public static AgentMessagePart text(String text) {
        return new AgentMessagePart("text", text);
    }
}

