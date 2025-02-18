package com.ecommerce.apigatewayservice.service.reactiveloadbalancer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.client.loadbalancer.reactive.ReactiveLoadBalancer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReactiveLoadBalancerService {

    private final ReactiveLoadBalancer.Factory<ServiceInstance> loadBalancerFactory;

    public Mono<ServiceInstance> chooseInstance(String serviceId) {
        return Mono.from(loadBalancerFactory.getInstance(serviceId)
                .choose())
                .map(Response::getServer)
                .doOnNext(instance -> log.info("Chose instance: {} for service: {}", instance, serviceId))
                .doOnError(error -> log.error("Error choosing instance for service: {}", serviceId, error));
    }
}
