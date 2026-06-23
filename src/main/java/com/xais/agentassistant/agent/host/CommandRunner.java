package com.xais.agentassistant.agent.host;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

public interface CommandRunner {
    CommandResult run(List<String> command, Path workingDirectory, Duration timeout) throws Exception;
}

