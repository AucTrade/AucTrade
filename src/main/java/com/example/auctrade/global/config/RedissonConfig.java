package com.example.auctrade.global.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.redisson.config.address}")
    private String address;
    @Value("${spring.redisson.config.password}")
    private String password;

    @Bean
    public RedissonClient redissonClient(){
        Config config = new Config();
        config.setCodec(new StringCodec());
        config.useSingleServer().setAddress(address);
        return Redisson.create(config);
    }
}