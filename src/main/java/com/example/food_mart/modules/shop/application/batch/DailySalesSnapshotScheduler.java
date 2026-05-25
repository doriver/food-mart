package com.example.food_mart.modules.shop.application.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class DailySalesSnapshotScheduler {

    private final JobOperator jobOperator;
    private final Job dailySalesSnapshotJob;

    public DailySalesSnapshotScheduler(JobOperator jobOperator,
                                       @Qualifier(DailySalesSnapshotJobConfig.JOB_NAME) Job dailySalesSnapshotJob) {
        this.jobOperator = jobOperator;
        this.dailySalesSnapshotJob = dailySalesSnapshotJob;
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void run() {
        try {
            JobParameters params = new JobParametersBuilder()
                    .addLocalDateTime("snapshotAt", LocalDateTime.now())
                    .toJobParameters();
            jobOperator.start(dailySalesSnapshotJob, params);
        } catch (Exception e) {
            log.error("dailySalesSnapshotJob failed", e);
        }
    }
}
