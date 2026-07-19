package com.studyshield.gateway.health;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;

@Component
public class HealthPrettyWebFilter implements WebFilter {

    private final ObjectMapper prettyMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (!"/actuator/health".equals(exchange.getRequest().getPath().value())) {
            return chain.filter(exchange);
        }

        ServerHttpResponse originalResponse = exchange.getResponse();

        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                if (body instanceof Flux) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    return ((Flux<? extends DataBuffer>) body)
                            .doOnNext(dataBuffer -> {
                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                try {
                                    buffer.write(content);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            })
                            .doOnComplete(() -> {
                                try {
                                    byte[] original = buffer.toByteArray();
                                    Object json = prettyMapper.readValue(original, Object.class);
                                    byte[] pretty = prettyMapper.writeValueAsBytes(json);
                                    getHeaders().setContentLength(pretty.length);
                                    getHeaders().setContentType(
                                            org.springframework.http.MediaType.APPLICATION_JSON);
                                    DataBuffer prettyBuffer = exchange.getResponse().bufferFactory().wrap(pretty);
                                    super.writeWith(Mono.just(prettyBuffer)).subscribe();
                                } catch (Exception e) {
                                    DataBuffer fallback = exchange.getResponse().bufferFactory()
                                            .wrap(buffer.toByteArray());
                                    super.writeWith(Mono.just(fallback)).subscribe();
                                }
                            })
                            .then();
                }
                return super.writeWith(body);
            }
        };

        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}
