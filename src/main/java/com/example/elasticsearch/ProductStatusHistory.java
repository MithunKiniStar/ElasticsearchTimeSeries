package com.example.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductStatusHistory {
    private static final String INDEX_NAME = "product_status_history";
    private final ElasticsearchClient client;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    public ProductStatusHistory() throws IOException {
        System.out.println("Initializing Elasticsearch client...");
        // Create the low-level client
        RestClient restClient = RestClient
                .builder(new HttpHost("localhost", 9200))
                .build();

        // Create the transport with a custom mapper
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(mapper));

        // Create the API client
        client = new ElasticsearchClient(transport);
        System.out.println("Elasticsearch client initialized successfully");
    }

    public void createIndex() throws IOException {
        System.out.println("Checking if index exists...");
        boolean indexExists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        
        if (indexExists) {
            System.out.println("Index already exists. Deleting it...");
            client.indices().delete(d -> d.index(INDEX_NAME));
            System.out.println("Index deleted successfully");
        }
        
        System.out.println("Creating status history index...");
        // Create the index with proper mapping for timestamp
        client.indices().create(c -> c
            .index(INDEX_NAME)
            .mappings(m -> m
                .properties("productId", p -> p.keyword(k -> k))
                .properties("status", p -> p.keyword(k -> k))
                .properties("timestamp", p -> p.keyword(k -> k))
            )
        );
        System.out.println("Status history index created successfully");
    }

    public void recordStatusChange(String productId, String status) throws IOException {
        System.out.println("Recording status change for product " + productId + " to " + status);
        // Create a status history document
        StatusHistoryEntry entry = new StatusHistoryEntry(productId, status, Instant.now());
        
        // Index the status change
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .document(entry));
        System.out.println("Status change recorded: " + response.id());
    }
    
    public void recordStatusChangeWithTimestamp(StatusHistoryEntry entry) throws IOException {
        System.out.println("Recording status change for product " + entry.productId() + 
                         " to " + entry.status() + " at " + entry.timestamp());
        
        // Convert Instant to LocalDateTime and format it
        LocalDateTime dateTime = LocalDateTime.ofInstant(entry.timestamp(), ZoneId.systemDefault());
        String formattedTimestamp = dateTime.format(DATE_FORMATTER);
        
        // Create the document with formatted timestamp string
        client.index(i -> i
            .index(INDEX_NAME)
            .id(entry.productId() + "_" + formattedTimestamp)
            .document(new StatusHistoryEntryWithString(
                entry.productId(),
                entry.status(),
                formattedTimestamp
            ))
        );
    }

    public void getStatusAtTime(String productId, Instant time) throws IOException {
        System.out.println("Getting status for product " + productId + " at time " + time);
        
        // Convert Instant to LocalDateTime and format it
        LocalDateTime dateTime = LocalDateTime.ofInstant(time, ZoneId.systemDefault());
        String formattedTime = dateTime.format(DATE_FORMATTER);
        
        SearchResponse<StatusHistoryEntryWithString> response = client.search(builder -> builder
            .index(INDEX_NAME)
            .query(q -> q
                .bool(b -> b
                    .must(m -> m
                        .term(t -> t
                            .field("productId")
                            .value(productId)
                        )
                    )
                    .must(m -> m
                        .range(r -> r
                            .field("timestamp")
                            .lte(JsonData.of(formattedTime))
                        )
                    )
                )
            )
            .sort(sort -> sort
                .field(f -> f
                    .field("timestamp")
                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Desc)
                )
            )
            .size(1),
            StatusHistoryEntryWithString.class
        );

        List<Hit<StatusHistoryEntryWithString>> hits = response.hits().hits();
        if (!hits.isEmpty()) {
            StatusHistoryEntryWithString entry = hits.get(0).source();
            System.out.println("Status at " + time + " was: " + entry.status());
        } else {
            System.out.println("No status found for product " + productId + " at time " + time);
        }
    }

    public void getStatusHistory(String productId) throws IOException {
        System.out.println("Getting complete status history for product " + productId);
        
        SearchResponse<StatusHistoryEntryWithString> response = client.search(builder -> builder
            .index(INDEX_NAME)
            .query(q -> q
                .term(t -> t
                    .field("productId")
                    .value(productId)
                )
            )
            .sort(sort -> sort
                .field(f -> f
                    .field("timestamp")
                    .order(co.elastic.clients.elasticsearch._types.SortOrder.Asc)
                )
            ),
            StatusHistoryEntryWithString.class
        );

        List<Hit<StatusHistoryEntryWithString>> hits = response.hits().hits();
        if (!hits.isEmpty()) {
            System.out.println("Status history for product " + productId + ":");
            for (Hit<StatusHistoryEntryWithString> hit : hits) {
                StatusHistoryEntryWithString entry = hit.source();
                System.out.println("Time: " + entry.timestamp() + ", Status: " + entry.status());
            }
        } else {
            System.out.println("No status history found for product " + productId);
        }
    }

    public record StatusHistoryEntry(
        String productId,
        String status,
        Instant timestamp
    ) {}

    public record StatusHistoryEntryWithString(
        String productId,
        String status,
        String timestamp
    ) {}
} 