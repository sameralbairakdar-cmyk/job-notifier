package com.samer.jobs.telegram;

import com.samer.jobs.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Service
public class TelegramService extends TelegramLongPollingBot {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.chat.id}")
    private String chatId;

    private final TelegramCommandHandler commandHandler;

    public TelegramService(TelegramCommandHandler commandHandler) {
        this.commandHandler = commandHandler;
    }

    @Override
    public String getBotToken() { return botToken; }

    @Override
    public String getBotUsername() { return botUsername; }

    // ─── Eingehende Nachrichten ───────────────────────
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            System.out.println("📨 Telegram command received: " + userMessage);
            String response = commandHandler.handle(userMessage);
            sendText(response);
        }
    }

    // ─── Neue Stelle senden ───────────────────────────
    public void sendJob(Job job, int score) {
        String stars = "⭐".repeat(Math.min(score / 2, 5));
        String text = String.format("""
            🚀 *Neue Stelle gefunden!* %s (%d/10)

            📌 *%s*
            🏢 %s
            📍 %s
            %s
            """,
            stars,
            score,
            escape(job.getTitle()),
            escape(job.getCompany() != null ? job.getCompany() : "N/A"),
            escape(job.getLocation() != null ? job.getLocation() : "N/A"),
            job.getLink() != null ? "\n🔗 " + job.getLink() : ""
        );
        sendText(text);
    }

    // ─── Text senden ─────────────────────────────────
    public void sendText(String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        message.setParseMode("Markdown");
        message.disableWebPagePreview();
        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("❌ Telegram send error: " + e.getMessage());
        }
    }

    // ─── Markdown-Sonderzeichen escapen ──────────────
    private String escape(String text) {
        if (text == null) return "N/A";
        return text.replace("_", "\\_").replace("*", "\\*").replace("`", "\\`");
    }
}
