package com.xais.agentassistant.agent.host;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

public final class ProcessCommandRunner implements CommandRunner {
    @Override
    public CommandResult run(List<String> command, Path workingDirectory, Duration timeout) throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory.toFile());
        }

        Process process = builder.start();
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        Thread stdoutThread = copyAsync(process.getInputStream(), stdout);
        Thread stderrThread = copyAsync(process.getErrorStream(), stderr);

        boolean finished = process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!finished) {
            process.destroyForcibly();
            process.waitFor(5, TimeUnit.SECONDS);
            stdoutThread.join(1000);
            stderrThread.join(1000);
            return new CommandResult(
                    124,
                    stdout.toString(StandardCharsets.UTF_8),
                    "Command timed out after " + timeout.toSeconds() + "s"
            );
        }

        stdoutThread.join(1000);
        stderrThread.join(1000);
        return new CommandResult(
                process.exitValue(),
                stdout.toString(StandardCharsets.UTF_8),
                stderr.toString(StandardCharsets.UTF_8)
        );
    }

    private static Thread copyAsync(java.io.InputStream input, ByteArrayOutputStream output) {
        Thread thread = new Thread(() -> {
            try (input; output) {
                input.transferTo(output);
            } catch (Exception ignored) {
            }
        }, "agent-command-output-copy");
        thread.setDaemon(true);
        thread.start();
        return thread;
    }
}

