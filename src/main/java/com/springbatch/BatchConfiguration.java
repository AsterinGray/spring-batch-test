package com.springbatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
public class BatchConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoffeeProcessor.class);
    @Value("${file.input}")
    private String fileInput;

    @Bean
    public FlatFileItemReader reader() {
        return new FlatFileItemReaderBuilder<>()
                .name("coffeeItemReader")
                .resource(new ClassPathResource(fileInput))
                .delimited()
                .names("brand", "origin", "characteristics")
                .fieldSetMapper(new BeanWrapperFieldSetMapper() {
                    {
                        setTargetType(CoffeeEntity.class);
                    }
                }).build();
    }

    @Bean
    public JdbcBatchItemWriter writer(DataSource dataSource) {
        LOGGER.info(dataSource.toString());
        return new JdbcBatchItemWriterBuilder<CoffeeEntity>()
                .itemSqlParameterSourceProvider(
                        new BeanPropertyItemSqlParameterSourceProvider<>()
                ).sql(
                        "INSERT INTO coffee (brand, origin, characteristics)" +
                                "VALUES (:brand, :origin, :characteristics)"
                ).dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(JobRepository jobRepository, JobCompletionNotificationListener listener, Step step1) {
        return new JobBuilder("importUserJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(listener)
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JobRepository jobRepository, PlatformTransactionManager transactionManager, JdbcBatchItemWriter<CoffeeEntity> writer) {
        return new StepBuilder("step1", jobRepository)
                .<CoffeeEntity, CoffeeEntity>chunk(10, transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }

    @Bean
    public CoffeeProcessor processor() {
        return new CoffeeProcessor();
    }
}
