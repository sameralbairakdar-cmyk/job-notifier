package com.samer.jobs.telegram;

import com.samer.jobs.ai.AIJobScoringService;
import org.springframework.stereotype.Component;

@Component
public class TelegramCommandHandler {

    private final AIJobScoringService aiService;

    public TelegramCommandHandler(AIJobScoringService aiService) {
        this.aiService = aiService;
    }

    public String handle(String userMessage) {

        // أوامر مباشرة
        if (userMessage.equalsIgnoreCase("/start")) {
            return """
                👋 *Job Notifier AI* ist aktiv!

                Verfügbare Befehle:
                /start  – Bot starten
                /status – System-Status
                /help   – Hilfe anzeigen

                Oder schreibe einfach: `java hannover` oder `full stack berlin`
                """;
        }

        if (userMessage.equalsIgnoreCase("/status")) {
            return "✅ Bot läuft. Nächste Suche in max. 10 Minuten.";
        }

        if (userMessage.equalsIgnoreCase("/help")) {
            return """
                🤖 *Job Notifier AI – Hilfe*

                Schreibe einfach was du suchst, z.B.:
                • `java developer`
                • `spring boot hannover`
                • `embedded systems`
                • `full stack berlin`

                Der Bot versteht natürliche Sprache auf Deutsch, Englisch und Arabisch.
                """;
        }

        // AI-gestützte Befehlsverarbeitung
        String prompt = """
            Der Nutzer hat folgende Nachricht an einen Job-Such-Bot geschrieben: "%s"

            Antworte NUR mit diesem JSON (kein anderer Text):
            {
              "action": "search" oder "unknown",
              "keywords": ["keyword1", "keyword2"],
              "location": "Stadt oder null",
              "response": "kurze freundliche Antwort auf Deutsch (max 2 Sätze)"
            }
            """.formatted(userMessage);

        String json = aiService.callClaude(prompt);

        try {
            int start = json.indexOf("\"response\"");
            if (start != -1) {
                int valueStart = json.indexOf("\"", start + 11) + 1;
                int valueEnd = json.indexOf("\"", valueStart);
                if (valueStart > 0 && valueEnd > valueStart) {
                    return json.substring(valueStart, valueEnd);
                }
            }
        } catch (Exception ignored) {}

        return "✅ Befehl empfangen: " + userMessage;
    }
}
