package com.xais.agentassistant.plugin;

import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public final class AgentAssistantToolWindowFactory implements ToolWindowFactory, DumbAware {
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        AgentAssistantWindow window = new AgentAssistantWindow(project);
        Content content = ContentFactory.getInstance().createContent(window.component(), "Chat", false);
        toolWindow.getContentManager().addContent(content);
    }
}

