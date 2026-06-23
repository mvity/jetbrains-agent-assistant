package com.xais.agentassistant.agent.adapters.codex;

import com.xais.agentassistant.agent.adapters.cli.AbstractCliRuntime;
import com.xais.agentassistant.agent.core.AgentCapabilities;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.host.CommandRunner;
import com.xais.agentassistant.agent.host.ExecutableResolver;

import java.util.List;

public final class CodexRuntime extends AbstractCliRuntime {
    public static final String ID = "codex";

    public CodexRuntime() {
        super();
    }

    public CodexRuntime(ExecutableResolver executableResolver, CommandRunner commandRunner) {
        super(executableResolver, commandRunner);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public AgentCapabilities capabilities() {
        return new AgentCapabilities(true, true, true, false, true, false, false, false, true);
    }

    @Override
    protected String displayName() {
        return "Codex";
    }

    @Override
    protected String defaultCommand() {
        return "codex";
    }

    @Override
    protected List<String> buildSendCommand(String executable, AgentSendRequest request, String prompt) {
        return List.of(
                executable,
                "exec",
                "--cd",
                request.sessionRef().projectRoot(),
                "--ask-for-approval",
                "never",
                "--sandbox",
                "read-only",
                prompt
        );
    }
}

