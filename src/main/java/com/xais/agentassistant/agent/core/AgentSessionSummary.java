package com.xais.agentassistant.agent.core;

import java.time.Instant;

public record AgentSessionSummary(
        AgentSessionRef sessionRef,
        String title,
        Instant createdAt,
        Instant updatedAt
) {
}

