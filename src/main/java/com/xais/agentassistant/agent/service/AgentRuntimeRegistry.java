package com.xais.agentassistant.agent.service;

import com.xais.agentassistant.agent.core.AgentRuntime;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class AgentRuntimeRegistry {
    private final Map<String, AgentRuntime> runtimes = new LinkedHashMap<>();

    public void register(AgentRuntime runtime) {
        runtimes.put(runtime.id(), runtime);
    }

    public Optional<AgentRuntime> find(String runtimeId) {
        return Optional.ofNullable(runtimes.get(runtimeId));
    }

    public AgentRuntime require(String runtimeId) {
        AgentRuntime runtime = runtimes.get(runtimeId);
        if (runtime == null) {
            throw new IllegalArgumentException("Unknown agent runtime: " + runtimeId);
        }
        return runtime;
    }
}

