/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.example;

import java.time.Duration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.JettyClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

public class ClientWebApp {

	private static final Logger logger = LogManager.getLogger(ClientWebApp.class);

	public static void main(String[] args) {
		logger.info("Starting");
		try {
			ResponseEntity<String> entity =
					WebClient.builder().clientConnector(new JettyClientHttpConnector()).build()
							.get().uri("http://localhost:8080/hello")
							.retrieve()
							.toEntity(String.class)
							.block(Duration.ofSeconds(5));
			logger.info(entity);
		}
		catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		}
		finally {
			System.exit(0);
		}
	}

}
