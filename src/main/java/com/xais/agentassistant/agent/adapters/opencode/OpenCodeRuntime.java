package com.xais.agentassistant.agent.adapters.opencode;

import com.xais.agentassistant.agent.adapters.cli.AbstractCliRuntime;
import com.xais.agentassistant.agent.core.AgentCapabilities;
import com.xais.agentassistant.agent.core.AgentSendRequest;
import com.xais.agentassistant.agent.host.CommandRunner;
import com.xais.agentassistant.agent.host.ExecutableResolver;

import java.util.List;

public final class OpenCodeRuntime extends AbstractCliRuntime {
    public static final String ID = "opencode";

    public OpenCodeRuntime() {
        super();
    }

    public OpenCodeRuntime(ExecutableResolver executableResolver, CommandRunner commandRunner) {
        super(executableResolver, commandRunner);
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public AgentCapabilities capabilities() {
        return new AgentCapabilities(true, true, true, false, true, true, true, false, false);
    }

    @Override
    protected String displayName() {
        return "OpenCode";
    }

    @Override
    protected String defaultCommand() {
        return "opencode";
    }

    @Override
    protected List<String> buildSendCommand(String executable, AgentSendRequest request, String prompt) {
        return List.of(
                executable,
                "run",
                "--pure",
                "--prompt",
                prompt
        );
    }
}
