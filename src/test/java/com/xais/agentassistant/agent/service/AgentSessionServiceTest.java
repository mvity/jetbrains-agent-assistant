package com.xais.agentassistant.agent.service;

import com.xais.agentassistant.agent.adapters.fake.FakeAgentRuntime;
import com.xais.agentassistant.agent.core.AgentEvent;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class AgentSessionServiceTest {
    @Test
    public void sendTextEmitsSessionDeltaAndDoneEvents() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        registry.register(new FakeAgentRuntime());
        AgentSessionService service = new AgentSessionService(registry, FakeAgentRuntime.ID);

        List<AgentEvent> events = new ArrayList<>();
        service.sendText("/tmp/project", "hello", events::add);

        assertEquals("agent.session.created", events.get(0).type());
        assertEquals("agent.message.delta", events.get(1).type());
        assertEquals("Fake agent received: hello", events.get(1).text());
        assertEquals("agent.done", events.get(2).type());
        assertTrue(events.stream().allMatch(event -> event.sessionRef() != null));
    }

    @Test
    public void secondMessageReusesCurrentSession() {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        registry.register(new FakeAgentRuntime());
        AgentSessionService service = new AgentSessionService(registry, FakeAgentRuntime.ID);

        List<AgentEvent> first = new ArrayList<>();
        service.sendText("/tmp/project", "first", first::add);

        List<AgentEvent> second = new ArrayList<>();
        service.sendText("/tmp/project", "second", second::add);

        assertEquals("agent.message.delta", second.get(0).type());
        assertEquals(first.get(0).sessionRef(), second.get(0).sessionRef());
    }
}

