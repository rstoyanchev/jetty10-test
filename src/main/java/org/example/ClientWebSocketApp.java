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

import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import reactor.core.publisher.Mono;

import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.JettyWebSocketClient;

public class ClientWebSocketApp {

	private static final Logger logger = LogManager.getLogger(ClientWebSocketApp.class);


	public static void main(String[] args) {
		JettyWebSocketClient client = new JettyWebSocketClient();
		client.start();

		String protocol = "echo-v1";
		AtomicReference<HandshakeInfo> infoRef = new AtomicReference<>();
		AtomicReference<Object> protocolRef = new AtomicReference<>();

		try {
			client.execute(URI.create("ws://localhost:8080/websocket"),
					new WebSocketHandler() {
						@Override
						public List<String> getSubProtocols() {
							return Collections.singletonList(protocol);
						}

						@Override
						public Mono<Void> handle(WebSocketSession session) {
							infoRef.set(session.getHandshakeInfo());
							return session.receive()
									.map(WebSocketMessage::getPayloadAsText)
									.doOnNext(protocolRef::set)
									.doOnError(protocolRef::set)
									.then();
						}
					})
					.block(Duration.ofSeconds(5));

			HandshakeInfo info = infoRef.get();
			logger.info("Handshake Info: " + info);
		}
		catch (Throwable ex) {
			logger.error(ex.getMessage(), ex);
		}
		finally {
			System.exit(0);
		}
	}

}
