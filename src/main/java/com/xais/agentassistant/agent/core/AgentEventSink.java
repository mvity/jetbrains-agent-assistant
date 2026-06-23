package com.xais.agentassistant.agent.core;

@FunctionalInterface
public interface AgentEventSink {
    void emit(AgentEvent event);
}

