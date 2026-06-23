package com.xais.agentassistant.agent.adapters.cli;

import com.xais.agentassistant.agent.core.AgentCapabilities;
import com.xais.agentassistant.agent.core.AgentEvent;
import com.xais.agentassistant.agent.core.AgentEventSink;
import com.xais.agentassistant.agent.core.AgentHealth;
import com.xais.agentassistant.agent.core.AgentMessage;
import com.xais.agentassistant.agent.core.AgentMessagePart;
import com.xais.agentassistant.agent.core.AgentPermissionDecision;
import com.xais.agentassistant.agent.core.AgentProjectRef;
import com.xais.agentassistant.agent.core.AgentRuntime;
import com.xais.agentassistant.agent.core.AgentRuntimeConfig;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.core.AgentSessionRef;
import com.xais.agentassistant.agent.core.AgentSessionSummary;
import com.xais.agentassistant.agent.core.AgentStartRequest;
import com.xais.agentassistant.agent.host.CommandResult;
import com.xais.agentassistant.agent.host.CommandRunner;
import com.xais.agentassistant.agent.host.ExecutableResolver;
import com.xais.agentassistant.agent.host.PathExecutableResolver;
import com.xais.agentassistant.agent.host.ProcessCommandRunner;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractCliRuntime implements AgentRuntime {
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);

    private final ExecutableResolver executableResolver;
    private final CommandRunner commandRunner;
    private final ConcurrentMap<String, AgentSessionRef> sessions = new ConcurrentHashMap<>();

    protected AbstractCliRuntime() {
        this(new PathExecutableResolver(), new ProcessCommandRunner());
    }

    protected AbstractCliRuntime(ExecutableResolver executableResolver, CommandRunner commandRunner) {
        this.executableResolver = executableResolver;
        this.commandRunner = commandRunner;
    }

    protected abstract String displayName();

    protected abstract String defaultCommand();

    protected abstract List<String> buildSendCommand(String executable, AgentSendRequest request, String prompt);

    @Override
    public abstract String id();

    @Override
    public abstract AgentCapabilities capabilities();

    @Override
    public AgentHealth health(AgentRuntimeConfig config) {
        String command = commandFrom(config);
        Optional<Path> executable = executableResolver.resolve(command);
        return executable.map(path -> AgentHealth.healthy(displayName() + " executable found: " + path)).orElseGet(() -> AgentHealth.unhealthy(displayName() + " executable not found: " + command));
    }

    @Override
    public AgentSessionRef startSession(AgentStartRequest request) {
        AgentSessionRef sessionRef = new AgentSessionRef(
                id(),
                "local-" + UUID.randomUUID(),
                request.projectRoot(),
                request.workspaceId()
        );
        sessions.put(sessionRef.providerSessionId(), sessionRef);
        return sessionRef;
    }

    @Override
    public void sendMessage(AgentSendRequest request, AgentEventSink sink) {
        Optional<Path> executable = executableResolver.resolve(defaultCommand());
        if (executable.isEmpty()) {
            sink.emit(AgentEvent.error(request.sessionRef(),
                    displayName() + " executable not found: " + defaultCommand()));
            sink.emit(AgentEvent.done(request.sessionRef()));
            return;
        }

        String prompt = extractText(request.message());
        Path cwd = request.sessionRef().projectRoot() == null || request.sessionRef().projectRoot().isBlank()
                ? null
                : Path.of(request.sessionRef().projectRoot());

        try {
            CommandResult result = commandRunner.run(
                    buildSendCommand(executable.get().toString(), request, prompt),
                    cwd,
                    DEFAULT_TIMEOUT
            );
            if (result.success()) {
                String output = result.stdout().trim();
                sink.emit(AgentEvent.messageDelta(request.sessionRef(), output.isEmpty() ? "(no output)" : output));
            } else {
                String error = !result.stderr().isBlank() ? result.stderr().trim() : result.stdout().trim();
                sink.emit(AgentEvent.error(request.sessionRef(), error.isBlank()
                        ? displayName() + " exited with code " + result.exitCode()
                        : error));
            }
        } catch (Exception e) {
            sink.emit(AgentEvent.error(request.sessionRef(), e.getMessage()));
        } finally {
            sink.emit(AgentEvent.done(request.sessionRef()));
        }
    }

    @Override
    public void abort(AgentSessionRef sessionRef) {
        // One-shot CLI execution does not maintain a long-lived process yet.
    }

    @Override
    public List<AgentSessionSummary> listSessions(AgentProjectRef projectRef) {
        return sessions.values().stream()
                .filter(ref -> projectRef.projectRoot().equals(ref.projectRoot()))
                .map(ref -> new AgentSessionSummary(ref, displayName() + " session", Instant.now(), Instant.now()))
                .toList();
    }

    @Override
    public List<AgentMessage> loadMessages(AgentSessionRef sessionRef) {
        return List.of();
    }

    @Override
    public void approvePermission(AgentPermissionDecision decision) {
        // Permission transports will be wired per runtime after process gateways are introduced.
    }

    @Override
    public void dispose(AgentSessionRef sessionRef) {
        sessions.remove(sessionRef.providerSessionId());
    }

    private String commandFrom(AgentRuntimeConfig config) {
        if (config == null || config.values() == null) {
            return defaultCommand();
        }
        return config.values().getOrDefault("command", defaultCommand());
    }

    private static String extractText(AgentMessage message) {
        return message.parts().stream()
                .filter(part -> "text".equals(part.type()))
                .map(AgentMessagePart::text)
                .findFirst()
                .orElse("");
    }
}

