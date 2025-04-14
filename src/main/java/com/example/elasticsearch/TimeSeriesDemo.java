package com.example.elasticsearch;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class TimeSeriesDemo {
    public static void main(String[] args) {
        try {
            System.out.println("Starting TimeSeriesDemo...");
            
            // Create the status history index
            System.out.println("Initializing ProductStatusHistory...");
            ProductStatusHistory statusHistory = new ProductStatusHistory();
            
            System.out.println("Creating index...");
            statusHistory.createIndex();

            // Record initial status for a product
            String productId = "1";
            System.out.println("\nRecording initial status...");
            statusHistory.recordStatusChange(productId, "ACTIVE");

            // Simulate some time passing
            System.out.println("Waiting 1 second...");
            Thread.sleep(1000);

            // Record status change
            System.out.println("\nRecording status change...");
            statusHistory.recordStatusChange(productId, "OUT_OF_STOCK");

            // Simulate some time passing
            System.out.println("Waiting 1 second...");
            Thread.sleep(1000);

            // Record another status change
            System.out.println("\nRecording another status change...");
            statusHistory.recordStatusChange(productId, "BACK_IN_STOCK");

            // Get the complete status history
            System.out.println("\nGetting complete status history:");
            statusHistory.getStatusHistory(productId);

            // Get status at a specific point in time (between first and second change)
            Instant queryTime = Instant.now().minus(1, ChronoUnit.SECONDS);
            System.out.println("\nGetting status at a specific time:");
            statusHistory.getStatusAtTime(productId, queryTime);

            System.out.println("\nDemo completed successfully!");

        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 