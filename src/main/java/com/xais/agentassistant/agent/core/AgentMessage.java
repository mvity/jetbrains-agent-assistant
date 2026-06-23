package com.xais.agentassistant.agent.core;

import java.time.Instant;
import java.util.List;

public record AgentMessage(
        String id,
        String role,
        List<AgentMessagePart> parts,
        Instant createdAt
) {
}

