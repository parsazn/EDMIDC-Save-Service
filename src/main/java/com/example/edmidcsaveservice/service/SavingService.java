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
import java.util.Iterator;
import java.util.Map;

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

    public void save(String node, String tag, String measurementName) throws JsonProcessingException {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create(url, token, org, bucket);
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode matrix = mapper.readTree(node);

        Point point = Point.measurement(measurementName);
        point.addTag(tag, tag);
        point.time(Instant.now().toEpochMilli(), WritePrecision.MS);
        if(matrix.isArray()){
            for (JsonNode arrayNode : matrix) {
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = arrayNode.fields();
                while(fieldsIterator.hasNext()){
                    Map.Entry<String, JsonNode> fields =fieldsIterator.next();
                        parseJson(point,  fields.getKey(), fields.getValue());
                }
            }
        }else {
            Iterator<Map.Entry<String, JsonNode>> fields = matrix.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                parseJson(point, next.getKey(), next.getValue());
            }
        }
        writeApi.writePoint(point);
    }

    private static void parseJson(Point point, String key, JsonNode node) throws JsonProcessingException {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fieldsIterator = objectNode.fields();
            while(fieldsIterator.hasNext()){
                Map.Entry<String, JsonNode> fields =fieldsIterator.next();
                try {
                    parseJson(point, key + "." + fields.getKey(), fields.getValue());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode arrayNode : node) {
                Iterator<Map.Entry<String, JsonNode>> fieldsIterator = arrayNode.fields();
                while(fieldsIterator.hasNext()){
                    Map.Entry<String, JsonNode> fields =fieldsIterator.next();
                    try {
                        parseJson(point, key + "." + fields.getKey(), fields.getValue());
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
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

