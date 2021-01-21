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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.server.WebHandler;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

public class ServerApp {

	private static final Logger logger = LogManager.getLogger(ServerApp.class);


	public static void main(String[] args) throws Exception {
		ApplicationContext context = new AnnotationConfigApplicationContext(WebConfig.class);
		HttpHandler handler = WebHttpHandlerBuilder.applicationContext(context).build();

		JettyHttpServer server = new JettyHttpServer();
		server.setHandler(handler);
		server.setPort(8080);
		server.afterPropertiesSet();
		server.start();

		logger.info("Up and running. Enter \"stop\", \"exit\", or \"quit\" to shut down.\n");
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String line = reader.readLine().toLowerCase();
			if (!Arrays.asList("stop", "exit", "quit").contains(line)) {
				continue;
			}
			logger.info("Shutting down and exiting");
			server.stop();
			System.exit(0);
		}
	}


	@Configuration
	@EnableWebFlux
	static class WebConfig implements WebFluxConfigurer {

		@Bean
		public HelloController helloController() {
			return new HelloController();
		}

		@Bean
		public HandlerMapping handlerMapping() {
			Map<String, Object> map = new HashMap<>();
			map.put("/hello-basic", (WebHandler) exchange -> {
				DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
				return exchange.getResponse().writeWith(Mono.just(toDataBuffer("Hi", bufferFactory)));
			});
			map.put("/websocket", (WebSocketHandler) session -> {
				String protocol = session.getHandshakeInfo().getSubProtocol();
				WebSocketMessage message = session.textMessage(protocol != null ? protocol : "none");
				return session.send(Mono.just(message));
			});
			return new SimpleUrlHandlerMapping(map);
		}

		@Bean
		@Order(0)
		public WebSocketHandlerAdapter webSocketHandlerAdapter() {
			return new WebSocketHandlerAdapter();
		}

		private DataBuffer toDataBuffer(String value, DataBufferFactory bufferFactory) {
			return bufferFactory.wrap(value.getBytes(StandardCharsets.UTF_8));
		}
	}


	@RestController
	static class HelloController {

		@GetMapping("/hello")
		String handle() {
			return "Hello from @MVC";
		}
	}

}
