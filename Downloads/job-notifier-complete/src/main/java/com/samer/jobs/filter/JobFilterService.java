package com.samer.jobs.filter;

import com.samer.jobs.model.Job;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JobFilterService {
    /*Exam*/
    private static final List<String> KEYWORDS = List.of(
        "java", "spring", "spring boot", "full stack", "fullstack",
        "backend", "software", "entwickler", "developer", "it ",
        "embedded", "kotlin", "microservice"
    );

    private static final List<String> EXCLUDE_KEYWORDS = List.of(
        "buchhalter", "verkäufer", "fahrer", "pfleger", "arzt",
        "lehrer", "küche", "reinigung"
    );

    public boolean isRelevant(Job job) {
        String text = buildSearchText(job);

        // ausschließen falls unpassend
        for (String exclude : EXCLUDE_KEYWORDS) {
            if (text.contains(exclude)) return false;
        }

        // prüfen ob relevantes Keyword vorhanden
        for (String keyword : KEYWORDS) {
            if (text.contains(keyword)) return true;
        }

        return false;
    }

    private String buildSearchText(Job job) {
        StringBuilder sb = new StringBuilder();
        if (job.getTitle() != null)       sb.append(job.getTitle()).append(" ");
        if (job.getDescription() != null) sb.append(job.getDescription()).append(" ");
        if (job.getCompany() != null)     sb.append(job.getCompany()).append(" ");
        return sb.toString().toLowerCase();
    }
}
