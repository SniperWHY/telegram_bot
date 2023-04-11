package com.honey.telegram_bot.service;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;

@Service
@Slf4j
public class ChatGPTService {

    public final OpenAiService service;

    public interface Callback {
        void send(ChatMessage message);
    }

    public interface CallbackText {
        void send(String text);
    }

    @Value("${openai.model}")
    private String model;

    @Value("${openai.maxToken}")
    private int maxToken;

    public ChatGPTService(@Value("${openai.apikey}") String apiKey, @Value("${openai.timeout}") Long timout) {
        this.service = new OpenAiService(apiKey, Duration.ofSeconds(30L));
    }

    public void getAnswer(Vector<ChatMessage> messages,
                          String prompt,
                          Callback callback,
                          CallbackText callbackText) {
        Vector<ChatMessage> messagesCopy = new Vector<>();
        int tokens = this.maxToken - prompt.length() - messages.get(0).getContent().length();
        messagesCopy.add(messages.get(0));
        Stack<ChatMessage> stack = new Stack<>();

        for (int i = messages.size() - 1; i >= 1; i--) {
            ChatMessage item = messages.get(i);
            if (tokens - item.getContent().length() >= 0) {
                tokens -= item.getContent().length();
                stack.push(messages.get(i));
            } else {
                break;
            }
        }
        while (!stack.isEmpty()) messagesCopy.add(stack.pop());
        messagesCopy.add(new ChatMessage("user", prompt));
        new Thread(() -> {
            try {
                ChatCompletionRequest request = ChatCompletionRequest
                        .builder()
                        .messages(messagesCopy)
                        .model(model)
                        .build();
                ChatCompletionResult res = this.service.createChatCompletion(request);
                res.getChoices().get(0).getMessage();
                callback.send(res.getChoices().get(0).getMessage());
            } catch (Exception e) {
                callbackText.send("I'm sorry, I've come across something");
                log.error("请求openai出现异常：{}", e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

}
