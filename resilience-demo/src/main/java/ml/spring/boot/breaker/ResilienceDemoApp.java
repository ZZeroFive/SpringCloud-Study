package ml.spring.boot.breaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Hello world!
 *
 */

@SpringBootApplication
public class ResilienceDemoApp {
    public static void main( String[] args ) {
        SpringApplication.run(ResilienceDemoApp.class, args);
    }
}
