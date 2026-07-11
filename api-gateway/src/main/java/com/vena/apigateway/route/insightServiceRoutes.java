package com.vena.apigateway.route;

import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.net.URI;

import static org.springframework.cloud.gateway.server.mvc.filter.BeforeFilterFunctions.uri;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;
import static org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions.http;

@Configuration
public class insightServiceRoutes {


    @Bean
    public RouterFunction<ServerResponse> insightRoute(){
        return route("insight-service")
                .route(RequestPredicates.path("/api/v1/insights/**"), http())
                .before(uri("http://localhost:8085"))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker("insightServiceCircuitBreaker",
                        URI.create("forward:/insightFallbackRoute")))
                .build();

    }

    @Bean
    public RouterFunction<ServerResponse> insightFallbackRoute(){
        return route("fallbackRoute")
                .route(RequestPredicates.path("/fallbackRoute"),
                        request -> ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Insight Service is currently unavailable. Please try again later."))
                .build();
    }
}
