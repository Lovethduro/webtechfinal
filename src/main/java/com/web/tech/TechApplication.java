package com.web.tech;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;


import java.net.ServerSocket;

@SpringBootApplication
@ComponentScan(basePackages = "com.web.tech")
public class TechApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechApplication.class, args);
	}

	@Bean
	public WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> webServerFactoryCustomizer() {
		return factory -> {
			int port = findAvailablePort(8080, 8100); // Try ports between 8080-8100
			factory.setPort(port);
			System.out.println("\n=========================================");
			System.out.println("  Glam Nexa Application Started!");
			System.out.println("  Landing Page: http://localhost:" + port + "/landing");
			System.out.println("=========================================\n");
		};
	}

	private static int findAvailablePort(int minPort, int maxPort) {
		for (int port = minPort; port <= maxPort; port++) {
			try (ServerSocket ignored = new ServerSocket(port)) {
				return port;
			} catch (Exception e) {
				// Port is in use, try next one
			}
		}
		throw new RuntimeException("No available port found in range " + minPort + "-" + maxPort);
	}
}