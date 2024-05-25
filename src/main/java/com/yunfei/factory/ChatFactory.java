package com.yunfei.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.yunfei.ChatUI;
import org.jetbrains.annotations.NotNull;


public class ChatFactory implements ToolWindowFactory {
    private ChatUI chatUi = new ChatUI();

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        // 获取内容工厂的实例
        ContentFactory contentFactory = ContentFactory.getInstance();
        // 获取 ToolWindow 显示的内容
        Content content = contentFactory.createContent(chatUi.getJComponent(), "", false);
        // 设置 ToolWindow 显示的内容
        toolWindow.getContentManager().addContent(content);
    }
}
