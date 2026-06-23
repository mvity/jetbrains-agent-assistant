package com.xais.agentassistant.plugin;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefJSQuery;
import com.xais.agentassistant.agent.adapters.claude.ClaudeRuntime;
import com.xais.agentassistant.agent.adapters.codex.CodexRuntime;
import com.xais.agentassistant.agent.adapters.fake.FakeAgentRuntime;
import com.xais.agentassistant.agent.adapters.opencode.OpenCodeRuntime;
import com.xais.agentassistant.agent.core.AgentEvent;
import com.xais.agentassistant.agent.service.AgentRuntimeRegistry;
import com.xais.agentassistant.agent.service.AgentSessionService;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AgentAssistantWindow {
    private static final Logger LOG = Logger.getInstance(AgentAssistantWindow.class);

    private final Project project;
    private final JPanel root = new JPanel(new BorderLayout());
    private final Gson gson = new Gson();
    private final AgentSessionService sessionService;
    private final ExecutorService agentExecutor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "agent-assistant-runtime");
        thread.setDaemon(true);
        return thread;
    });
    private JBCefBrowser browser;

    public AgentAssistantWindow(Project project) {
        this.project = project;

        AgentRuntimeRegistry registry = new AgentRuntimeRegistry();
        registry.register(new FakeAgentRuntime());
        registry.register(new ClaudeRuntime());
        registry.register(new CodexRuntime());
        registry.register(new OpenCodeRuntime());
        this.sessionService = new AgentSessionService(registry, FakeAgentRuntime.ID);

        createContent();
    }

    public JComponent component() {
        return root;
    }

    private void createContent() {
        if (!JBCefBrowser.isSupported()) {
            root.add(new JLabel("JCEF is not supported in this IDE runtime."), BorderLayout.CENTER);
            return;
        }

        browser = new JBCefBrowser();
        JBCefJSQuery query = JBCefJSQuery.create(browser);
        query.addHandler(message -> {
            handleWebviewMessage(message);
            return new JBCefJSQuery.Response("ok");
        });

        root.add(browser.getComponent(), BorderLayout.CENTER);
        browser.loadHTML(loadHtml(query));
    }

    private String loadHtml(JBCefJSQuery query) {
        String html;
        try (InputStream input = getClass().getResourceAsStream("/webview/index.html")) {
            if (input == null) {
                return "<html><body>Missing /webview/index.html</body></html>";
            }
            html = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.warn("Failed to load webview HTML: " + e.getMessage());
            return "<html><body>Failed to load Agent Assistant UI.</body></html>";
        }

        String bridgeScript = "<script>"
                + "window.sendToJava = function(message) { "
                + query.inject("message", "function(){ }", "function(){ }")
                + "; };"
                + "</script>";
        return html.replace("</body>", bridgeScript + "</body>");
    }

    private void handleWebviewMessage(String message) {
        try {
            JsonObject payload = gson.fromJson(message, JsonObject.class);
            if (payload == null || !payload.has("type")) {
                return;
            }
            String type = payload.get("type").getAsString();
            if ("sendMessage".equals(type)) {
                String text = payload.has("text") ? payload.get("text").getAsString() : "";
                String runtimeId = payload.has("runtimeId")
                        ? payload.get("runtimeId").getAsString()
                        : FakeAgentRuntime.ID;
                agentExecutor.submit(() -> sessionService.sendText(runtimeId, projectRoot(), text, this::emitToWebview));
            }
        } catch (Exception e) {
            LOG.warn("Failed to handle webview message: " + e.getMessage());
            emitToWebview(AgentEvent.error(null, e.getMessage()));
        }
    }

    private String projectRoot() {
        String basePath = project.getBasePath();
        return basePath != null ? basePath : "";
    }

    private void emitToWebview(AgentEvent event) {
        if (browser == null || event == null) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            String json = gson.toJson(WebviewAgentEvent.from(event));
            browser.getCefBrowser().executeJavaScript(
                    "window.agentAssistantReceiveEvent(" + json + ");",
                    browser.getCefBrowser().getURL(),
                    0
            );
        });
    }

    private record WebviewAgentEvent(
            String type,
            String runtimeId,
            String providerSessionId,
            String text,
            String message,
            String createdAt
    ) {
        private static WebviewAgentEvent from(AgentEvent event) {
            return new WebviewAgentEvent(
                    event.type(),
                    event.sessionRef() != null ? event.sessionRef().runtimeId() : null,
                    event.sessionRef() != null ? event.sessionRef().providerSessionId() : null,
                    event.text(),
                    event.message(),
                    event.createdAt().toString()
            );
        }
    }
}
