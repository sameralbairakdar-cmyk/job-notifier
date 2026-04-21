package com.samer.jobs.model;

import org.springframework.data.jpa.repository.JpaRepository;

public interface JobRepository extends JpaRepository<Job, Long> {
    boolean existsByUniqueKey(String uniqueKey);
}
