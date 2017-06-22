package com.onedrive;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

@SpringBootApplication
public class SpringBootWebApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(SpringBootWebApplication.class);
	}

	public static void main(String[] args) throws Exception {
		//SpringApplication.run(SpringBootWebApplication.class, args);
		
		 SpringApplicationBuilder builder = new SpringApplicationBuilder(SpringBootWebApplication.class);
		    builder.headless(false)
		  
		    .run(args);
	}

}