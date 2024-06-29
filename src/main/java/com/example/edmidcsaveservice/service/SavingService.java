package com.example.edmidcsaveservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import org.springframework.stereotype.Service;

import java.time.Instant;

//https://stackoverflow.com/questions/41183756/inserting-list-as-value-in-influxdb
@Service
public class SavingService {

    //TODO take token automatically
    private static char[] token = "qWrHmLNor5vcAGTeUqf57H98l1p6D1kDkAtXdOBlS0mQ235iKO0k_Gcpinjy0EqlMnIhwoZNcwSXm_WLxEDQtg==".toCharArray();
    private static String org = "admin";
    private static String bucket = "admin";

    public void save(JsonNode node) {

        InfluxDBClient influxDBClient = InfluxDBClientFactory.create("http://localhost:8086", token, org, bucket);
        WriteApiBlocking writeApi = influxDBClient.getWriteApiBlocking();

        Point point = Point.measurement("test");
        point.addTag("s", "Kun");
        point.time(Instant.now().toEpochMilli(), WritePrecision.MS);

        node.fields().forEachRemaining(x -> parseJson(point,x.getKey(),x.getValue()));

        writeApi.writePoint(point);
    }

    private static void parseJson(Point point, String key , JsonNode node) {
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            objectNode.fields().forEachRemaining(x -> parseJson(point,x.getKey(),x.getValue()));
        } else if (node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for (JsonNode jsonNode : arrayNode) {
                jsonNode.fields().forEachRemaining(x -> parseJson(point,x.getKey(),x.getValue()));
            }
        } else if (node.isTextual()) {
            point.addField(key,node.asText());
        } else if (node.isInt()) {
            point.addField(key,node.asInt());
        } else if (node.isBoolean()) {
            point.addField(key,node.asBoolean());
        } else if (node.isDouble()) {
            point.addField(key,node.asDouble());
        } else {
            point.addField(key,node.asText());
        }
    }

}

