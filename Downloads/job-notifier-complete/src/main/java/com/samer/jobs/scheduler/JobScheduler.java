package com.samer.jobs.scheduler;

import com.samer.jobs.ai.AIJobScoringService;
import com.samer.jobs.filter.JobFilterService;
import com.samer.jobs.model.Job;
import com.samer.jobs.model.JobRepository;
import com.samer.jobs.scraper.JobScraperService;
import com.samer.jobs.telegram.TelegramService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class JobScheduler {

    private final JobScraperService scraper;
    private final JobFilterService filter;
    private final JobRepository repository;
    private final TelegramService telegram;
    private final AIJobScoringService aiScoring;

    public JobScheduler(JobScraperService scraper,
                        JobFilterService filter,
                        JobRepository repository,
                        TelegramService telegram,
                        AIJobScoringService aiScoring) {
        this.scraper    = scraper;
        this.filter     = filter;
        this.repository = repository;
        this.telegram   = telegram;
        this.aiScoring  = aiScoring;
    }

    @Scheduled(fixedDelayString = "${scheduler.delay:600000}")
    public void run() {
        System.out.println("─────────────────────────────────────");
        System.out.println("🔍 Job search started...");

        List<Job> jobs = scraper.scrape();
        System.out.println("📋 Total scraped: " + jobs.size());

        int sent = 0, skipped = 0, duplicate = 0;

        for (Job job : jobs) {

            // 1. Keyword-Filter (schnell, kostenlos)
            if (!filter.isRelevant(job)) {
                skipped++;
                continue;
            }

            // 2. Duplicate-Check
            String key = buildKey(job);
            job.setUniqueKey(key);
            if (repository.existsByUniqueKey(key)) {
                duplicate++;
                continue;
            }

            // 3. AI Scoring (Claude API)
            int score = aiScoring.scoreJob(job);
            System.out.printf("   %s → score %d/10%n", job.getTitle(), score);

            // 4. Nur Stellen mit Score >= 6 senden
            if (score >= 6) {
                telegram.sendJob(job, score);
                repository.save(job);
                sent++;
            } else {
                skipped++;
            }
        }

        System.out.printf("✅ Sent: %d | ⏭ Skipped: %d | 🔁 Duplicates: %d%n",
            sent, skipped, duplicate);
        System.out.println("─────────────────────────────────────");
    }

    private String buildKey(Job job) {
        String raw = (job.getTitle() != null ? job.getTitle() : "")
                   + (job.getCompany() != null ? job.getCompany() : "");
        return raw.toLowerCase().replaceAll("[^a-z0-9]", "");
    }
}
