package com.honey.telegram_bot.bots;
import com.honey.telegram_bot.service.BotService;
import com.honey.telegram_bot.util.MarkdownEscapeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
@Scope("singleton")
public class ChatSpecialist extends TelegramLongPollingBot {

    @Value("${telegramBot.username}")
    private String username;

    private final BotService botService;

    public ChatSpecialist(DefaultBotOptions options, @Value("${telegramBot.token}") String token, BotService botService) {
        super(options, token);
        this.botService = botService;
        this.botService.setSenderText(this::sendText);
    }

    @Override
    public String getBotUsername() {
        return this.username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        this.botService.processUpdate(update);
    }

    public void sendText(Long chatId, String text, Integer replyId) {
        try {
            SendMessage message = new SendMessage();
            message.setChatId(chatId);
            message.setText(MarkdownEscapeUtil.escapeMarkdown(text));
            if (replyId != null)
                message.setReplyToMessageId(replyId);
            message.enableMarkdownV2(true);
            execute(message);
        } catch (Exception e) {
            log.error("Error while sending message", e);
        }
    }
}
