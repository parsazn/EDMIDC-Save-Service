package com.example.edmidcsaveservice.configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class InfluxDBConfig {
    @Value("${spring.influxdb.config.url}")
    private String url;
    @Value("${spring.influxdb.config.token}")
    private String token;
    @Value("${spring.influxdb.config.org}")
    private String org;
    @Value("${spring.influxdb.config.bucket}")
    private String bucket;
}
