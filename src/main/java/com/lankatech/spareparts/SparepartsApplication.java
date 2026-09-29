package com.lankatech.spareparts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

@SpringBootApplication
public class SparepartsApplication {

	public static void main(String[] args) {
		SpringApplication.run(SparepartsApplication.class, args);
	}

	@EventListener(ApplicationReadyEvent.class)
	public void openBrowser() {
		try {
			new ProcessBuilder(
					"cmd",
					"/c",
					"start",
					"",
					"http://localhost:8080/"
			).start();

			System.out.println("Browser opened successfully.");
		} catch (Exception e) {
			System.out.println("Browser could not be opened automatically.");
			e.printStackTrace();
		}
	}
}