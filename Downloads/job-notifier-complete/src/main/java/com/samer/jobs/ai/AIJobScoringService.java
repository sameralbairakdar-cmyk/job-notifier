package com.samer.jobs.ai;

import com.samer.jobs.model.Job;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class AIJobScoringService {

    @Value("${claude.api.key}")
    private String apiKey;

    private WebClient getClient() {
        return WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("x-api-key", apiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    // ─── تقييم الوظيفة من 1 إلى 10 ──────────────────
    public int scoreJob(Job job) {
        String prompt = """
            Bewerte diese Stelle für einen Java Full-Stack Developer (Hannover, Deutschland):

            Titel: %s
            Firma: %s
            Standort: %s
            Beschreibung: %s

            Antworte NUR mit diesem JSON (kein anderer Text):
            {"score": <1-10>, "reason": "<kurze Begründung>"}

            Bewertungsskala:
            9-10 = perfekt (Java, Spring Boot, Full-Stack)
            6-8  = gut (Backend, Software, IT)
            3-5  = akzeptabel (allgemeine IT)
            1-2  = nicht passend
            """.formatted(
                job.getTitle(),
                job.getCompany() != null ? job.getCompany() : "N/A",
                job.getLocation() != null ? job.getLocation() : "N/A",
                job.getDescription() != null
                    ? job.getDescription().substring(0, Math.min(500, job.getDescription().length()))
                    : "N/A"
        );

        String response = callClaude(prompt);

        try {
            int scoreIdx = response.indexOf("\"score\"");
            if (scoreIdx != -1) {
                String after = response.substring(scoreIdx + 8).replaceAll("[^0-9]", "");
                if (!after.isEmpty()) {
                    int score = Integer.parseInt(after.substring(0, 1));
                    return Math.min(10, Math.max(1, score));
                }
            }
        } catch (Exception e) {
            System.err.println("⚠ Score parsing failed, defaulting to 5");
        }
        return 5;
    }

    // ─── استدعاء Claude API ───────────────────────────
    public String callClaude(String prompt) {
        Map<String, Object> body = Map.of(
            "model", "claude-sonnet-4-20250514",
            "max_tokens", 300,
            "messages", List.of(
                Map.of("role", "user", "content", prompt)
            )
        );

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = getClient().post()
                .uri("/v1/messages")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

            if (response != null) {
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
                if (content != null && !content.isEmpty()) {
                    return (String) content.get(0).get("text");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Claude API error: " + e.getMessage());
        }
        return "{\"score\": 5}";
    }
}
