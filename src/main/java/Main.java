import com.example.elasticsearch.ElasticsearchDemo;
import com.example.elasticsearch.Product;

import java.io.IOException;

public class Main {

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
