package com.example.edmidcsaveservice.service;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import org.junit.jupiter.api.Test;

import java.time.Instant;



public class SavingServiceTest {

    private static char[] token = "qWrHmLNor5vcAGTeUqf57H98l1p6D1kDkAtXdOBlS0mQ235iKO0k_Gcpinjy0EqlMnIhwoZNcwSXm_WLxEDQtg==".toCharArray();
    private static String org = "admin";
    private static String bucket = "admin";

    @Test
    public void test() {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);

        //
        // Write data
        //
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("testBitCoin")
                .addTag("Euro", "Asia")
                .addField("value", 69)
                .time(Instant.now().toEpochMilli(), WritePrecision.MS);

        writeApi.writePoint(point);

    }
}