package com.yunfei;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.models.QwenParam;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.ResultCallback;
import com.alibaba.dashscope.common.Role;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.concurrent.Semaphore;

/**
 * @author houyunfei
 */
public class ChatUI {
    private JTabbedPane tabbedPane1;
    private JPanel panel1;
    private JTextField question;
    private JButton sendMessageButton;
    private JEditorPane editorPane1;
    public static String key = "你的通义千问key";

    public JComponent getJComponent() {
        return tabbedPane1;
    }

    public ChatUI() {
        sendMessageButton.addActionListener((ActionEvent e) -> {
            String text = question.getText();
            editorPane1.setContentType("text/html");
            editorPane1.setText("Loading...");
            new SwingWorker<Void, String>() {
                private StringBuilder fullContent = new StringBuilder();
                private Semaphore semaphore = new Semaphore(0);

                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        Generation gen = new Generation();
                        Message userMsg = Message.builder().role(Role.USER.getValue()).content(text).build();
                        QwenParam param =
                                QwenParam.builder().model("qwen-14b-chat")
                                        .messages(Arrays.asList(userMsg))
                                        .apiKey(key)
                                        .resultFormat(QwenParam.ResultFormat.MESSAGE)
                                        .topP(0.8)
                                        .incrementalOutput(true) // get streaming output incrementally
                                        .build();
                        gen.streamCall(param, new ResultCallback<GenerationResult>() {

                            @Override
                            public void onEvent(GenerationResult message) {
                                fullContent.append(message.getOutput().getChoices().get(0).getMessage().getContent());
                                publish(fullContent.toString());
                                System.out.println(message);
                            }

                            @Override
                            public void onError(Exception ex) {
                                System.out.println(ex.getMessage());
                                semaphore.release();
                            }

                            @Override
                            public void onComplete() {
                                System.out.println("onComplete");
                                semaphore.release();
                            }

                        });
                        semaphore.acquire();
                        System.out.println("Full content: \n" + fullContent);

                    } catch (Exception ex) {
                        publish("An error occurred while calling the generation service: " + ex.getMessage());
                        throw new RuntimeException(ex);
                    }
                    return null;
                }

                @Override
                protected void process(java.util.List<String> chunks) {
                    String markdown = chunks.get(chunks.size() - 1);
                    String html = convertMarkdownToHtml(markdown);
                    editorPane1.setText(html);
                }

                @Override
                protected void done() {
                    try {
                        get();
                    } catch (Exception ex) {
                        editorPane1.setText("An error occurred while calling the generation service: " + ex.getMessage());
                    }
                }
            }.execute();
        });
    }

    private String convertMarkdownToHtml(String markdown) {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();
        return renderer.render(parser.parse(markdown));
    }
}
