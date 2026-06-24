package com.vena.usageservice.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class InfluxdbConfig {

    @Value("${influx.url}")
    private String influxUrl;
    @Value("${influx.token}")
    private String influxToken;
    @Value("${influx.org}")
    private String influxOrg;


    @Bean
    public InfluxDBClient influxDBClient(){
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg);
    }
}
