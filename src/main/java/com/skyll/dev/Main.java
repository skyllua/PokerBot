package com.skyll.dev;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiRequestException;

import java.io.File;
import java.io.IOException;


public class Main {
    public static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi bot = new TelegramBotsApi();

        try {
            bot.registerBot(new Bot());
            System.out.println("-- bot is started");
            logger.info("bot is started");
        } catch (TelegramApiRequestException e) {
            e.printStackTrace();
            logger.trace(e.getStackTrace().toString());
        }
    }

}
