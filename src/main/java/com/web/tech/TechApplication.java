package com.web.tech;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;

@SpringBootApplication
@ComponentScan(basePackages = "com.web.tech")
public class TechApplication {

	@Autowired
	private Environment environment;

	@Value("${server.port:8080}")
	private String serverPort;

	public static void main(String[] args) {
		// Explicitly set the port from environment variable
		String port = System.getenv("PORT");
		if (port != null && !port.isEmpty()) {
			System.setProperty("server.port", port);
			System.out.println("Setting server port to: " + port);
		}
		SpringApplication.run(TechApplication.class, args);
	}

	@EventListener
	public void handleContextRefresh(ContextRefreshedEvent event) {
		String baseUrl = getBaseUrl();
		System.out.println("\n=========================================");
		System.out.println("  Glam Nexa Application Started!");
		System.out.println("  Landing Page: " + baseUrl + "/landing");
		System.out.println("=========================================\n");
	}

	private String getBaseUrl() {
		// Check if running on Heroku
		String herokuAppName = System.getenv("HEROKU_APP_NAME");
		if (herokuAppName != null && !herokuAppName.isEmpty()) {
			return "https://" + herokuAppName + ".herokuapp.com";
		}

		// Check for custom domain (if you have one)
		String customDomain = System.getenv("CUSTOM_DOMAIN");
		if (customDomain != null && !customDomain.isEmpty()) {
			return "https://" + customDomain;
		}

		// Default to localhost for local development
		return "http://localhost:" + serverPort;
	}
}