#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
OUT_DIR="${TMPDIR:-/tmp}/agent-assistant-core-classes"

rm -rf "$OUT_DIR"
mkdir -p "$OUT_DIR"

javac -d "$OUT_DIR" $(find "$ROOT_DIR/src/main/java/com/xais/agentassistant/agent" -name '*.java')

cat > "$OUT_DIR/AgentCoreSmoke.java" <<'JAVA'
import com.xais.agentassistant.agent.adapters.fake.FakeAgentRuntime;
import com.xais.agentassistant.agent.adapters.claude.ClaudeRuntime;
import com.xais.agentassistant.agent.adapters.codex.CodexRuntime;
import com.xais.agentassistant.agent.adapters.opencode.OpenCodeRuntime;
import com.xais.agentassistant.agent.core.AgentEvent;
import com.xais.agentassistant.agent.core.AgentHealth;
import com.xais.agentassistant.agent.core.AgentRuntimeConfig;
import com.xais.agentassistant.agent.host.CommandResult;
import com.xais.agentassistant.agent.service.AgentRuntimeRegistry;
import com.xais.agentassistant.agent.service.AgentSessionService;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class AgentCoreSmoke {
    public static void main(String[] args) {
        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        registry.register(new FakeAgentRuntime());
        AgentSessionService service = new AgentSessionService(registry, FakeAgentRuntime.ID);

        List<AgentEvent> events = new ArrayList<>();
        service.sendText("/tmp/project", "hello", events::add);

        if (events.size() != 3) {
            throw new AssertionError("Expected 3 events, got " + events.size());
        }
        if (!"agent.session.created".equals(events.get(0).type())) {
            throw new AssertionError("Missing session-created event");
        }
        if (!"Fake agent received: hello".equals(events.get(1).text())) {
            throw new AssertionError("Unexpected delta: " + events.get(1).text());
        }
        if (!"agent.done".equals(events.get(2).type())) {
            throw new AssertionError("Missing done event");
        }

        var fakeRunner = (com.xais.agentassistant.agent.host.CommandRunner) (command, workingDirectory, timeout) ->
            new CommandResult(0, "transport ok: " + String.join(" ", command), "");

        OpenCodeRuntime unavailableOpenCode = new OpenCodeRuntime(executable -> Optional.empty(), fakeRunner);
        AgentHealth unavailable = unavailableOpenCode.health(AgentRuntimeConfig.empty());
        if (unavailable.healthy()) {
            throw new AssertionError("OpenCode should be unhealthy when executable is missing");
        }

        OpenCodeRuntime availableOpenCode = new OpenCodeRuntime(
            executable -> Optional.of(Path.of("/usr/local/bin/opencode")),
            fakeRunner
        );
        AgentHealth available = availableOpenCode.health(new AgentRuntimeConfig(Map.of("command", "opencode")));
        if (!available.healthy()) {
            throw new AssertionError("OpenCode should be healthy when executable is resolved");
        }

        ClaudeRuntime claude = new ClaudeRuntime(
            executable -> Optional.of(Path.of("/usr/local/bin/claude")),
            fakeRunner
        );
        if (!claude.health(AgentRuntimeConfig.empty()).healthy()) {
            throw new AssertionError("Claude should be healthy when executable is resolved");
        }

        CodexRuntime codex = new CodexRuntime(
            executable -> Optional.of(Path.of("/usr/local/bin/codex")),
            fakeRunner
        );
        if (!codex.health(AgentRuntimeConfig.empty()).healthy()) {
            throw new AssertionError("Codex should be healthy when executable is resolved");
        }

        List<AgentEvent> codexEvents = new ArrayList<>();
        registry.register(codex);
        service.sendText(CodexRuntime.ID, "/tmp/project", "hello codex", codexEvents::add);
        if (!"agent.message.delta".equals(codexEvents.get(1).type())) {
            throw new AssertionError("Codex transport should emit a message delta");
        }
        if (!codexEvents.get(1).text().contains("codex exec")) {
            throw new AssertionError("Codex transport should use codex exec");
        }
    }
}
JAVA

javac -cp "$OUT_DIR" -d "$OUT_DIR" "$OUT_DIR/AgentCoreSmoke.java"
java -cp "$OUT_DIR" AgentCoreSmoke

echo "Agent core smoke test passed"
