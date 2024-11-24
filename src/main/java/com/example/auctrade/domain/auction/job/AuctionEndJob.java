package com.example.auctrade.domain.auction.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.repeat.CompletionPolicy;
import org.springframework.batch.repeat.policy.CompositeCompletionPolicy;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.policy.TimeoutTerminationPolicy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.*;

import static com.example.auctrade.global.constant.Constants.AUCTION_END_JOB_NAME;
import static com.example.auctrade.global.constant.Constants.REDIS_DEPOSIT_KEY;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class AuctionEndJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager platformTransactionManager;
    private final DataSource dataSource;
    private final RedissonClient redisClient;

    private static final String AUCTION_END_STEP = "auction_end_step";

    private static final int WORKER_SIZE = 2;

    @Value("${spring.batch.chunk-size}")
    private int chunkSize;

    @Bean
    public Job jdbcJob() {
        return new JobBuilder(AUCTION_END_JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(auctionEndStep())
                .build();
    }

    /**
     * DB 에서 auctionId를 꺼내온 후 해당하는 id의 예치금을 모두 취소하고 해당 데이터의 is_ended 값을 true로 전환
     */
    @JobScope
    @Bean
    public Step auctionEndStep() {
        return new StepBuilder(AUCTION_END_STEP, jobRepository)
                .<Long, AuctionDataSet>chunk(completionPolicy(), new DataSourceTransactionManager(dataSource))
                .reader(jdbcPagingItemReader())
                .processor(itemProcessor())
                .writer(this::updateChunkDataToDB)
                .transactionManager(platformTransactionManager)
                .taskExecutor(taskExecutor())
                .listener(jobExecutionListener(taskExecutor()))
                .build();
    }

    /**
     * DB 에서 종료 시간이 지난 경매 중 종료되지 않은 경매 Id를 Paging 형식으로 가져오기
     * Paging: multi - thread 에 대해 안전
     */
    @Bean
    public JdbcPagingItemReader<Long> jdbcPagingItemReader() {
        return new JdbcPagingItemReaderBuilder<Long>()
                .name("jdbcPageItemReader")
                .dataSource(dataSource)
                .pageSize(chunkSize)
                .queryProvider(createQueryProvider())
                .rowMapper(((rs, rowNum) -> rs.getLong(1)))
                .build();
    }

    private PagingQueryProvider createQueryProvider() {
        SqlPagingQueryProviderFactoryBean providerFactoryBean = new SqlPagingQueryProviderFactoryBean();
        providerFactoryBean.setDataSource(dataSource);
        providerFactoryBean.setSelectClause("select a.id");
        providerFactoryBean.setFromClause("from auction a");
        providerFactoryBean.setWhereClause("WHERE a.is_ended = false AND a.end_at <= NOW()");
        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.ASCENDING);
        providerFactoryBean.setSortKeys(sortKeys);

        try {
            return providerFactoryBean.getObject();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * chunk Item Processor
     * */
    @Bean
    public ItemProcessor<Long, AuctionDataSet> itemProcessor() {
        return id -> {
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            String query = "SELECT id, user_id, amount FROM deposit_log WHERE auction_id = ? AND status = 'CREATE'";

            List<DepositDataSet> depositDataSetList = jdbcTemplate.query(query, new Object[]{id}, (rs, rowNum) ->
                    new DepositDataSet(rs.getLong("id"), rs.getLong("user_id"), rs.getInt("amount"))
            );

            return new AuctionDataSet(id, depositDataSetList);
        };
    }
    /**
     * chunk writer
     * */
    private void updateChunkDataToDB(Chunk<? extends AuctionDataSet> chunkData) {
        NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

        // DB에서 is_ended 업데이트
        List<Long> auctionIds = chunkData.getItems().stream().map(AuctionDataSet::getAuctionId).toList();
        String updateAuctionQuery = "UPDATE auction SET is_ended = true WHERE id IN (:ids)";
        Map<String, Object> params = Collections.singletonMap("ids", auctionIds);
        jdbcTemplate.update(updateAuctionQuery, params);

        //DepositLog 상태 update
        String param = "logIds";
        String updateDepositLogQuery = "UPDATE deposit_log SET status = 'CANCEL' WHERE id IN (:" + param + ")";
        Map<String, Object> depositLogParams = cancelDepositLog(chunkData, param);
        if (depositLogParams != null) jdbcTemplate.update(updateDepositLogQuery, depositLogParams);

        // User point update
        String updateUserQuery = "UPDATE users SET point = point + :amount WHERE id = :userId";
        List<Map<String, Object>> batchParams = addUserPoint(chunkData);
        if (batchParams != null) jdbcTemplate.batchUpdate(updateUserQuery, batchParams.toArray(new Map[0]));

        // Redis에서 데이터를 삭제
        RBatch batch = redisClient.createBatch();
        for (AuctionDataSet dataSet : chunkData.getItems()) {
            batch.getBucket(REDIS_DEPOSIT_KEY + dataSet.getAuctionId()).deleteAsync();
        }
        batch.execute();
    }

    private Map<String, Object> cancelDepositLog(Chunk<? extends AuctionDataSet> chunkData, String param){
        List<Long> depositLogIds = new ArrayList<>();
        for (AuctionDataSet auctionData : chunkData.getItems()) {
                depositLogIds.addAll(auctionData.getDepositDataSetList().stream().map(DepositDataSet::getDepositLogId).toList());
        }
        return depositLogIds.isEmpty() ? null : Collections.singletonMap(param, depositLogIds);
    }

    private List<Map<String, Object>> addUserPoint(Chunk<? extends AuctionDataSet> chunkData){
        List<Map<String, Object>> batchParams = new ArrayList<>();
        for (AuctionDataSet auctionData : chunkData.getItems()) {
            for (DepositDataSet depositData : auctionData.getDepositDataSetList()) {
                Map<String, Object> paramsMap = new HashMap<>();
                paramsMap.put("amount", depositData.getAmount());
                paramsMap.put("userId", depositData.getUserId());
                batchParams.add(paramsMap);
            }
        }
        return batchParams.isEmpty() ? null : batchParams;
    }

    /**
     * chunk 단위 멀티스레드 설정
     * */
    @Bean
    public TaskExecutor taskExecutor(){
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();
        threadPoolTaskExecutor.setCorePoolSize(WORKER_SIZE);
        threadPoolTaskExecutor.setMaxPoolSize(WORKER_SIZE);
        threadPoolTaskExecutor.setThreadNamePrefix("exe-");
        threadPoolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskExecutor.setAllowCoreThreadTimeOut(true);
        threadPoolTaskExecutor.setKeepAliveSeconds(1);
        threadPoolTaskExecutor.initialize();
        return threadPoolTaskExecutor;
    }

    /**
     *  작업 종료시 ThreadPool 닫기
     */
    public JobExecutionListener jobExecutionListener(TaskExecutor taskExecutor){
            return new JobExecutionListener() {
                @Override
                public void beforeJob(JobExecution jobExecution) {
                    log.info("Job Start");
                }
                @Override
                public void afterJob(JobExecution jobExecution) {
                    ((ThreadPoolTaskExecutor) taskExecutor).shutdown();
                }
            };
    }

    /**
     *  작업이 완료되었는지 여부를 판단하는 데 사용되는 규칙
     */
    private CompletionPolicy completionPolicy() {
        CompositeCompletionPolicy policy =
                new CompositeCompletionPolicy();
        policy.setPolicies(
                new CompletionPolicy[] {
                        new TimeoutTerminationPolicy(3000),
                        new SimpleCompletionPolicy(chunkSize)});
        return policy;
    }
}