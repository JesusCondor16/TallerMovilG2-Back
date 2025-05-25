package com.pe.fisi.sw.cooperApp.cooperApp_discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class CooperAppDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(CooperAppDiscoveryApplication.class, args);
	}

}
