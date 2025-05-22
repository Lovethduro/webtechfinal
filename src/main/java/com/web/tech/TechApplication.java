package com.web.tech;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@SpringBootApplication
@ComponentScan(basePackages = "com.web.tech")
public class TechApplication {

	public static void main(String[] args) {
		// Explicitly set the port from environment variable
		String port = System.getenv("PORT");
		if (port != null && !port.isEmpty()) {
			System.setProperty("server.port", port);
			System.out.println("Binding to port: " + port);  // Log the port to confirm
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
		String herokuAppName = System.getenv("HEROKU_APP_NAME");
		if (herokuAppName != null && !herokuAppName.isEmpty()) {
			return "https://" + herokuAppName + ".herokuapp.com";
		}

		String customDomain = System.getenv("CUSTOM_DOMAIN");
		if (customDomain != null && !customDomain.isEmpty()) {
			return "https://" + customDomain;
		}

		return "http://localhost:" + System.getenv("PORT");
	}
}
