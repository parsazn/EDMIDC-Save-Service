package com.example.edmidcsaveservice.service;

import com.example.edmidcsaveservice.configuration.InfluxDBConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;

//https://stackoverflow.com/questions/41183756/inserting-list-as-value-in-influxdb
@Service
public class SavingService {

    private final char[] token;
    private final String org;
    private final String bucket;
    private final String url;

    @Autowired
    public SavingService(InfluxDBConfig influxDBConfig) {
        this.token = influxDBConfig.getToken().toCharArray();
        this.org = influxDBConfig.getOrg();
        this.bucket = influxDBConfig.getBucket();
        this.url = influxDBConfig.getUrl();
    }

    public void save(String node , String tag , String measurementName) throws JsonProcessingException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode matrix = mapper.readValue(node, JsonNode.class);

        Point point = Point.measurement(measurementName);
        point.addTag(tag,tag);
        point.time(Instant.now().toEpochMilli(), WritePrecision.MS);

        matrix.forEach(jsonNode ->
                jsonNode.fields().forEachRemaining(x -> {
                    try {
                        parseJson(point, x.getKey(), x.getValue());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                })
        );
        writeApi.writePoint(point);
    }

    private static void parseJson(Point point, String key, JsonNode node) throws JsonProcessingException {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(x -> {
                try {
                    parseJson(point, key + "." + x.getKey(), x.getValue());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            });
        } else if (node.isArray()) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode matrix = mapper.readValue(node.toString(), JsonNode.class);
            matrix.forEach(x -> x.fields().forEachRemaining(y -> {
                try {
                    parseJson(point, key + "." + y.getKey(), y.getValue());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }));
        } else if (node.isTextual()) {
            point.addField(key, node.asText());
        } else if (node.isInt()) {
            point.addField(key, node.asInt());
        } else if (node.isBoolean()) {
            point.addField(key, node.asBoolean());
        } else if (node.isDouble()) {
            point.addField(key, node.asDouble());
        } else {
            point.addField(key, node.asText());
        }
    }

}

