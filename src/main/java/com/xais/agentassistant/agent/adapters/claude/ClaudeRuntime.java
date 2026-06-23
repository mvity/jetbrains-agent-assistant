package com.xais.agentassistant.agent.adapters.claude;

import com.xais.agentassistant.agent.adapters.cli.AbstractCliRuntime;
import com.xais.agentassistant.agent.core.AgentCapabilities;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.host.CommandRunner;
import com.xais.agentassistant.agent.host.ExecutableResolver;

import java.util.List;

public final class ClaudeRuntime extends AbstractCliRuntime {
    public static final String ID = "claude";

    public ClaudeRuntime() {
        super();
    }

    public ClaudeRuntime(ExecutableResolver executableResolver, CommandRunner commandRunner) {
        super(executableResolver, commandRunner);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public AgentCapabilities capabilities() {
        return new AgentCapabilities(true, true, true, true, true, true, true, true, false);
    }

    @Override
    protected String displayName() {
        return "Claude";
    }

    @Override
    protected String defaultCommand() {
        return "claude";
    }

    @Override
    protected List<String> buildSendCommand(String executable, AgentSendRequest request, String prompt) {
        return List.of(
                executable,
                "--print",
                "--output-format",
                "text",
                "--permission-mode",
                "plan",
                prompt
        );
    }
}

