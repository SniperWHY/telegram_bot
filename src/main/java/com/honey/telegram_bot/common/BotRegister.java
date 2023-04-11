package com.honey.telegram_bot.common;

import com.honey.telegram_bot.bots.ChatSpecialist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class BotRegister {

    private static final Logger logger = LoggerFactory.getLogger(BotRegister.class);

    public static void boot(ApplicationContext context) {
        try {
            logger.info("start register bot");
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(context.getBean(ChatSpecialist.class));
            logger.info("register bot success");
        } catch (TelegramApiException e) {
            logger.error("register bot error", e);
            throw new RuntimeException(e);
        }
    }
}
