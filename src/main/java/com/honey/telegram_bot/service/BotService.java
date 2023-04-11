package com.honey.telegram_bot.service;

import com.theokanning.openai.completion.chat.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.*;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Scope("singleton")
@Slf4j
public class BotService {

    private static final Map<Long, Vector<ChatMessage>> chatContexts = new ConcurrentHashMap<>();
    private static final Map<Long, Integer> treadCounter = new ConcurrentHashMap<>();

    private final ChatGPTService chatGPTService;
    public interface SenderText {
        void send(Long chatId, String text, Integer replyId);
    }

    public BotService(ChatGPTService chatGPTService) {
        this.chatGPTService = chatGPTService;
    }

    @Value("${app.token}")
    private String token;

    @Value("${app.maxSession}")
    private int maxSessionLength;

    @Value("${app.maxThread}")
    private int maxThread;

    public SenderText senderText;

    public void processUpdate(Update update) {
        log.info("update: {}", update.getMessage().getText());
        Message message = update.getMessage();
        User user = message.getFrom();
        Long userId = user.getId();
        Long chatId = message.getChatId();
        Vector<ChatMessage> context = this.filter(message, user, chatId);
        if (context == null) return;
        this.userCreateThread(userId);
        this.chatGPTService.getAnswer(context, message.getText(), (res) -> {
            senderText.send(chatId, res.getContent(), message.getMessageId());
            context.add(res);
            this.userThreadStop(userId);
        }, text -> {
            senderText.send(chatId, text, message.getMessageId());
            this.userThreadStop(userId);
            context.add(new ChatMessage("system", "There was a problem with our server just now and we must apologize to the users"));
        });
    }

    // 某个用户占用的线程停止
    private void userThreadStop(Long userId) {
        // 此用户正在运行的线程数量
        int counter = treadCounter.get(userId);
        treadCounter.remove(userId);
        treadCounter.put(userId, -- counter);
    }

    // 某个用户创建线程
    private void userCreateThread(Long userId) {
        int counter = treadCounter.get(userId);
        treadCounter.remove(userId);
        treadCounter.put(userId, ++ counter);
    }

    // 过滤消息
    public Vector<ChatMessage> filter (Message message, User user, Long chatId) {
        Long userId = user.getId();
        // 用户全名
        String fullName = user.getLastName() + " " + user.getLastName();
        if (!message.hasText()) { // 非文本或音频过滤
            log.warn("只支持文本和音频警告：用户ID:{}, 用户名字：{}, 用户username: {}", user.getId(), fullName, user.getUserName());
            this.senderText.send(chatId, "Non text messages are temporarily not supported", null);
            return null;
        }

        String userSendText = message.getText();

        // 如果用户没有接入
        if (!chatContexts.containsKey(userId)) {
            // 判断输入的是否与设定的token一致
            if (userSendText.equals(this.token)) {
                if (maxSessionLength <= chatContexts.size()) {
                    log.warn("用户会话已满 用户ID:{}, 用户名字：{}, 用户username: {}", user.getId(), fullName, user.getUserName());
                    this.senderText.send(chatId, "Session is full", null);
                    return null;
                }
                // 输入正确加入聊天
                ChatMessage initMessage = new ChatMessage();
                initMessage.setRole("system");
                initMessage.setContent("Your name is Wise Man, and you are a very intelligent person. You need to try your best to serve your users. His name is " + fullName + "You must reply to all messages from the user in the language in which they first spoke to you, Do not show that you are an AI robot in the dialogue with the user");
                Vector<ChatMessage> context = new Vector<>();
                context.add(initMessage);
                chatContexts.put(userId, context);
                treadCounter.put(userId, 0);
                senderText.send(chatId, "It's good of you to come", null);
            } else {
                log.warn("用户输入的token错误：token:[{}] 用户ID:{}, 用户名字：{}, 用户username: {}", userSendText, user.getId(), fullName, user.getUserName());
                this.senderText.send(chatId, "Please enter the correct token", null);
            }
            return null;
        }

        if (treadCounter.get(userId) >= maxThread) {
            log.warn("线程超出最大限制：token:[{}] 用户ID:{}, 用户名字：{}, 用户username: {}", userSendText, user.getId(), fullName, user.getUserName());
            this.senderText.send(chatId, "Please wait", null);
            return null;
        }

        // 命令匹配
        return switch (userSendText) {
            case "/clear" -> {
                if (treadCounter.get(userId) != 0) {
                    log.warn("清除历史记录阻塞： 用户ID:{}, 用户名字：{}, 用户username: {}", user.getId(), fullName, user.getUserName());
                    this.senderText.send(chatId, "Please wait", null);
                    yield null;
                }
                ChatMessage head = chatContexts.get(userId).get(0);
                chatContexts.remove(userId);
                Vector<ChatMessage> _ctx = new Vector<>();
                _ctx.add(head);
                chatContexts.put(userId, _ctx);
                this.senderText.send(chatId, "cleared", null);
                yield null;
            }
            default -> chatContexts.get(user.getId());
        };
    }

    public void setSenderText(SenderText senderText) {
        this.senderText = senderText;
    }
}
