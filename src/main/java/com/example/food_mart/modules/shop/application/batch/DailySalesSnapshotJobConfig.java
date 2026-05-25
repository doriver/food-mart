package com.example.food_mart.modules.shop.application.batch;

import com.example.food_mart.modules.order.domain.mapper.DailySalesRow;
import com.example.food_mart.modules.order.domain.mapper.OrderItemMapper;
import com.example.food_mart.modules.shop.domain.entity.ItemDailySalesSnapshot;
import com.example.food_mart.modules.shop.domain.repository.ItemDailySalesSnapshotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.infrastructure.item.data.RepositoryItemWriter;
import org.springframework.batch.infrastructure.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Configuration
@RequiredArgsConstructor
public class DailySalesSnapshotJobConfig {

    static final String JOB_NAME = "dailySalesSnapshotJob";
    private static final String STEP_NAME = "dailySalesSnapshotStep";
    private static final int CHUNK_SIZE = 50;

    private final OrderItemMapper orderItemMapper;
    private final ItemDailySalesSnapshotRepository snapshotRepository;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean(JOB_NAME)
    public Job dailySalesSnapshotJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(dailySalesSnapshotStep())
                .build();
    }

    @Bean(STEP_NAME)
    public Step dailySalesSnapshotStep() {
        LocalDateTime snapshotAt = LocalDateTime.now();
        LocalDate yesterday = snapshotAt.toLocalDate().minusDays(1);
        LocalDateTime from = yesterday.atStartOfDay();
        LocalDateTime to = snapshotAt.toLocalDate().atStartOfDay();

        return new StepBuilder(STEP_NAME, jobRepository)
                .<DailySalesRow, ItemDailySalesSnapshot>chunk(CHUNK_SIZE)
                .reader(new DailySalesSnapshotItemReader(orderItemMapper, from, to))
                .processor(new DailySalesSnapshotItemProcessor(yesterday, snapshotAt))
                .writer(dailySalesSnapshotItemWriter())
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public RepositoryItemWriter<ItemDailySalesSnapshot> dailySalesSnapshotItemWriter() {
        return new RepositoryItemWriterBuilder<ItemDailySalesSnapshot>()
                .repository(snapshotRepository)
                .methodName("saveAll")
                .build();
    }
}
