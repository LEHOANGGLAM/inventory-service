package com.yes4all.service.cronjob;

import com.yes4all.service.IssueNoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ResetSequenceCron {

    private final Logger log = LoggerFactory.getLogger(ResetSequenceCron.class);

    @Autowired
    IssueNoteService issueNoteService;

    /**
     * Every month (zone time LosAngeles), reset sequence.
     *
     */
    @Scheduled(cron = "0 0 0 1 * *", zone = "America/Los_Angeles")
    public void executeCron() {
        try {
            log.debug("Cron reset sequence");
            // Call repository reset sequence
            issueNoteService.resetSequence();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
