package com.samer.jobs.scraper;

import com.samer.jobs.model.Job;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
public class JobScraperService {

    public List<Job> scrape() {
        List<Job> jobs = new ArrayList<>();
        WebDriver driver = null;

        try {
            driver = buildDriver();
            jobs.addAll(scrapeStepstone(driver));
            // يمكن إضافة مواقع أخرى هنا
            // jobs.addAll(scrapeIndeed(driver));
        } catch (Exception e) {
            System.err.println("❌ Scraping error: " + e.getMessage());
        } finally {
            if (driver != null) driver.quit();
        }

        return jobs;
    }

    // ─── Stepstone.de ────────────────────────────────
    private List<Job> scrapeStepstone(WebDriver driver) {
        List<Job> jobs = new ArrayList<>();
        String url = "https://www.stepstone.de/jobs/java-developer/";

        try {
            driver.get(url);
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

            // قبول الـ cookies إذا ظهرت
            try {
                WebElement acceptBtn = driver.findElement(
                    By.xpath("//button[contains(text(),'Alle akzeptieren') or contains(text(),'Accept')]")
                );
                acceptBtn.click();
                Thread.sleep(1000);
            } catch (Exception ignored) {}

            // انتظار تحميل الوظائف
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("[data-testid='job-item'], article.sc-beySbM, .res-1s7j06t")
            ));

            List<WebElement> jobCards = driver.findElements(
                By.cssSelector("[data-testid='job-item'], article[class*='JobItem'], .res-1s7j06t")
            );

            System.out.println("📋 Found " + jobCards.size() + " job cards on Stepstone");

            for (WebElement card : jobCards) {
                try {
                    Job job = new Job();

                    // Title
                    String title = extractText(card, new String[]{
                        "[data-testid='job-item-title']",
                        "h2", "h3", ".sc-beySbM"
                    });
                    job.setTitle(title);

                    // Company
                    String company = extractText(card, new String[]{
                        "[data-testid='job-item-company-name']",
                        ".sc-jXbUNg", "[class*='company']"
                    });
                    job.setCompany(company);

                    // Location
                    String location = extractText(card, new String[]{
                        "[data-testid='job-item-location']",
                        "[class*='location']", "[class*='Location']"
                    });
                    job.setLocation(location);

                    // Link
                    try {
                        WebElement link = card.findElement(By.tagName("a"));
                        job.setLink(link.getAttribute("href"));
                    } catch (Exception ignored) {}

                    // Description (نبني وصفاً من العنوان والشركة)
                    job.setDescription(title + " at " + company + " in " + location);

                    if (title != null && !title.isBlank()) {
                        jobs.add(job);
                    }

                } catch (Exception e) {
                    System.err.println("⚠ Error parsing job card: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("❌ Stepstone scraping failed: " + e.getMessage());
        }

        return jobs;
    }

    // ─── Helper: محاولة عدة selectors ────────────────
    private String extractText(WebElement parent, String[] selectors) {
        for (String selector : selectors) {
            try {
                WebElement el = parent.findElement(By.cssSelector(selector));
                String text = el.getText().trim();
                if (!text.isBlank()) return text;
            } catch (Exception ignored) {}
        }
        return "N/A";
    }

    // ─── Driver Setup ────────────────────────────────
    private WebDriver buildDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-gpu");
        options.addArguments("--window-size=1920,1080");
        options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        return new ChromeDriver(options);
    }
}
