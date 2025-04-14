package com.example.elasticsearch;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class ProductStatusScenarioDemo {
    public static void main(String[] args) {
        try {
            System.out.println("Starting ProductStatusScenarioDemo...");

            // Create the status history index
            System.out.println("Initializing ProductStatusHistory...");
            ProductStatusHistory statusHistory = new ProductStatusHistory();

            System.out.println("Creating index...");
            statusHistory.createIndex();

            // Define the product ID
            String productId = "1"; // iPhone 13

            // Define the dates for status changes
            LocalDateTime april1 = LocalDateTime.of(2025, 4, 1, 10, 0); // April 1, 2025, 10:00 AM
            LocalDateTime april3 = LocalDateTime.of(2025, 4, 3, 15, 30); // April 3, 2025, 3:30 PM
            LocalDateTime april8 = LocalDateTime.of(2025, 4, 8, 9, 15);  // April 8, 2025, 9:15 AM
            LocalDateTime april10 = LocalDateTime.of(2025, 4, 10, 14, 0); // April 10, 2025, 2:00 PM
            LocalDateTime May1 = LocalDateTime.of(2025, 5, 1, 0, 0); // May 1, 2025, 00:00 AM



            // Convert LocalDateTime to Instant
            Instant april1Instant = april1.atZone(ZoneId.systemDefault()).toInstant();
            Instant april3Instant = april3.atZone(ZoneId.systemDefault()).toInstant();
            Instant april8Instant = april8.atZone(ZoneId.systemDefault()).toInstant();
            Instant april10Instant = april10.atZone(ZoneId.systemDefault()).toInstant();
            Instant may1Instant = May1.atZone(ZoneId.systemDefault()).toInstant();
            // Record status changes with specific timestamps
            System.out.println("\nRecording status changes with timestamps...");

            // April 1: Status is "New"
            System.out.println("April 1, 2025: Status is 'New'");
            ProductStatusHistory.StatusHistoryEntry entry1 = new ProductStatusHistory.StatusHistoryEntry(productId, "New", april1Instant);
            statusHistory.recordStatusChangeWithTimestamp(entry1);

            // April 3: Status changes to "Trending"
            System.out.println("April 3, 2025: Status changes to 'Trending'");
            ProductStatusHistory.StatusHistoryEntry entry2 = new ProductStatusHistory.StatusHistoryEntry(productId, "Trending", april3Instant);
            statusHistory.recordStatusChangeWithTimestamp(entry2);

            // April 8: Status changes to "Old"
            System.out.println("April 8, 2025: Status changes to 'Old'");
            ProductStatusHistory.StatusHistoryEntry entry3 = new ProductStatusHistory.StatusHistoryEntry(productId, "Old", april8Instant);
            statusHistory.recordStatusChangeWithTimestamp(entry3);

            // May 1: Status changes to "Trending"
            System.out.println("May 1, 2025: Status changes to 'Trending'");
            ProductStatusHistory.StatusHistoryEntry entry4 = new ProductStatusHistory.StatusHistoryEntry(productId, "Trending", may1Instant);
            statusHistory.recordStatusChangeWithTimestamp(entry4);

            // Get the complete status history
            System.out.println("\nGetting complete status history:");
            statusHistory.getStatusHistory(productId);

            // Get status at April 10 (should be "Old")
            System.out.println("\nGetting status on April 10, 2025:");
            statusHistory.getStatusAtTime(productId, april10Instant);

            // Get status at April 5 (should be "Trending")
            LocalDateTime april5 = LocalDateTime.of(2025, 4, 5, 12, 0); // April 5, 2025, 12:00 PM
            Instant april5Instant = april5.atZone(ZoneId.systemDefault()).toInstant();
            System.out.println("\nGetting status on April 5, 2025:");
            statusHistory.getStatusAtTime(productId, april5Instant);

            // Get status at April 2 (should be "New")
            LocalDateTime april2 = LocalDateTime.of(2025, 4, 2, 18, 0); // April 2, 2025, 6:00 PM
            Instant april2Instant = april2.atZone(ZoneId.systemDefault()).toInstant();
            System.out.println("\nGetting status on April 2, 2025:");
            statusHistory.getStatusAtTime(productId, april2Instant);


            // Get status at May 2 (should be "Trending")
            LocalDateTime may2 = LocalDateTime.of(2025, 5, 2, 18, 0); // May 2, 2025, 6:00 PM
            Instant may2Instant = may2.atZone(ZoneId.systemDefault()).toInstant();
            System.out.println("\nGetting status on May 2, 2025:");
            statusHistory.getStatusAtTime(productId, may2Instant);

            System.out.println("\nDemo completed successfully!");

        } catch (IOException e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 