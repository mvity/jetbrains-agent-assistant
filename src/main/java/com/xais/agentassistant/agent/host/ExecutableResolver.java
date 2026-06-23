package com.xais.agentassistant.agent.host;

import java.nio.file.Path;
import java.util.Optional;

public interface ExecutableResolver {
    Optional<Path> resolve(String executableName);
}

