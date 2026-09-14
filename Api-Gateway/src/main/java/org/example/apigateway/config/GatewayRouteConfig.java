package org.example.apigateway.config;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

                // ===== ĐÃ SỬA LỖI =====
                // Trước (SAI):  .uri("http://localhost:8083")
                //   -> user-service chạy port ngẫu nhiên (server.port: 0), KHÔNG bao giờ ở 8083
                //   -> Gateway forward tới localhost:8083 -> Connection refused.
                // Sau  (ĐÚNG): .uri("lb://user-service")
                //   -> định tuyến qua LoadBalancer + Eureka theo service-id "user-service",
                //      Eureka tự phân giải ra host:port thực tế (dù ngẫu nhiên).
                .route("user-route", r -> r
                        .path("/api/users/**")
                        .uri("lb://user-service")
                )

                .build();
    }
}
