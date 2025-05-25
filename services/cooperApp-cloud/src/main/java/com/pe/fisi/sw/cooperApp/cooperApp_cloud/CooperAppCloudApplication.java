package com.pe.fisi.sw.cooperApp.cooperApp_cloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;
@SpringBootApplication
@EnableConfigServer
public class CooperAppCloudApplication {

	public static void main(String[] args) {
		SpringApplication.run(CooperAppCloudApplication.class, args);
	}

}
