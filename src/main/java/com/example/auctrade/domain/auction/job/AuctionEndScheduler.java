package com.example.auctrade.domain.auction.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.example.auctrade.global.constant.Constants.AUCTION_END_JOB_NAME;

@Slf4j(topic = "Scheduler Start : ")
@Component
public class AuctionEndScheduler {
    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    public AuctionEndScheduler(JobLauncher jobLauncher, JobRegistry jobRegistry){
        this.jobLauncher = jobLauncher;
        this.jobRegistry = jobRegistry;
    }
    /**
     * 특정 시간마다 경매 종료를 위한 batch 작업 실행
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정마다 실행
    public void runJob() {
        try {
            jobLauncher.run(jobRegistry.getJob(AUCTION_END_JOB_NAME), new JobParametersBuilder()
                            .addString("time", LocalDateTime.now().toString())
                            .toJobParameters());
        } catch (NoSuchJobException | JobInstanceAlreadyCompleteException | JobExecutionAlreadyRunningException |
                     JobParametersInvalidException | JobRestartException e) {
                throw new RuntimeException(e);
        }
    }
}
