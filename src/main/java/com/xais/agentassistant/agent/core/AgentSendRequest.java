package com.xais.agentassistant.agent.core;

public record AgentSendRequest(
        AgentSessionRef sessionRef,
        AgentMessage message
) {
}

