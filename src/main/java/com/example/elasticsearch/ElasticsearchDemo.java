package com.example.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;

import java.io.IOException;
import java.util.List;

public class ElasticsearchDemo {
    private static final String INDEX_NAME = "products";
    private final ElasticsearchClient client;

    public ElasticsearchDemo() {
        // Create the low-level client
        RestClient restClient = RestClient
                .builder(new HttpHost("localhost", 9200))
                .build();

        // Create a custom ObjectMapper with JavaTimeModule
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Create the transport with a custom Jackson mapper
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper(objectMapper));

        // Create the API client
        this.client = new ElasticsearchClient(transport);
    }

    public void createIndex() throws IOException {
        // Check if index exists
        boolean indexExists = client.indices().exists(e -> e.index(INDEX_NAME)).value();
        
        if (!indexExists) {
            // Create the index
            client.indices().create(c -> c.index(INDEX_NAME));
            System.out.println("Index created successfully");
        } else {
            System.out.println("Index already exists");
        }
    }

    public void indexProduct(Product product) throws IOException {
        // Index the product
        IndexResponse response = client.index(i -> i
                .index(INDEX_NAME)
                .id(product.getId())
                .document(product));
        System.out.println("Product indexed successfully: " + response.id());
    }

    public void searchProducts(String searchText) throws IOException {
        // Search for products using a more flexible query
        SearchResponse<Product> response = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q
                        .queryString(qs -> qs
                                .query("*" + searchText + "*")
                                .fields("name", "description")
                        )
                ),
                Product.class
        );

        // Print search results
        List<Hit<Product>> hits = response.hits().hits();
        System.out.println("Found " + hits.size() + " products:");
        for (Hit<Product> hit : hits) {
            System.out.println(hit.source());
        }
        
        // Print total hits for debugging
        System.out.println("Total hits: " + response.hits().total().value());
    }
    
    // Add a method to get all products for debugging
    public void getAllProducts() throws IOException {
        SearchResponse<Product> response = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.matchAll(ma -> ma)),
                Product.class
        );
        
        List<Hit<Product>> hits = response.hits().hits();
        System.out.println("All products in index (" + hits.size() + "):");
        for (Hit<Product> hit : hits) {
            System.out.println(hit.source());
        }
    }

    public static void main(String[] args) {
        try {
            ElasticsearchDemo demo = new ElasticsearchDemo();

            // Create index
            demo.createIndex();

            // Index some sample products
            Product product1 = new Product("1", "iPhone 13", "Latest Apple iPhone with amazing camera", 999.99, "Electronics");
            Product product2 = new Product("2", "Samsung Galaxy S21", "Android smartphone with great features", 899.99, "Electronics");
            Product product3 = new Product("3", "MacBook Pro", "Powerful laptop for professionals", 1299.99, "Computers");

            demo.indexProduct(product1);
            demo.indexProduct(product2);
            demo.indexProduct(product3);

            // Get all products to verify indexing
            System.out.println("\nVerifying all indexed products:");
            demo.getAllProducts();

            // Search for products
            System.out.println("\nSearching for 'phone':");
            demo.searchProducts("phone");

            System.out.println("\nSearching for 'laptop':");
            demo.searchProducts("laptop");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 