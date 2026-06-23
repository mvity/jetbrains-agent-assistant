package com.xais.agentassistant.agent.host;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public final class PathExecutableResolver implements ExecutableResolver {
    @Override
    public Optional<Path> resolve(String executableName) {
        if (executableName == null || executableName.isBlank()) {
            return Optional.empty();
        }

        Path direct = Path.of(executableName);
        if (direct.isAbsolute() || executableName.contains("/") || executableName.contains("\\")) {
            return isExecutableFile(direct) ? Optional.of(direct) : Optional.empty();
        }

        String path = System.getenv("PATH");
        if (path == null || path.isBlank()) {
            return Optional.empty();
        }

        for (String entry : path.split(java.io.File.pathSeparator)) {
            Path candidate = Path.of(entry, executableName);
            if (isExecutableFile(candidate)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    private static boolean isExecutableFile(Path path) {
        return Files.isRegularFile(path) && Files.isExecutable(path);
    }
}

