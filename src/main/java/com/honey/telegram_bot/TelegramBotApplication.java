package com.honey.telegram_bot;

import com.honey.telegram_bot.common.BotRegister;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TelegramBotApplication {

    public static void main(String[] args) {
        BotRegister.boot(SpringApplication.run(TelegramBotApplication.class, args));
    }

}
