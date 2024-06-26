package com.ecommerce.apigatewayservice.service.loadbalancer;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LoadBalancerService {

    private final LoadBalancerClient loadBalancerClient;

    public int getPortNumber(String serviceId) {
        return loadBalancerClient.choose(serviceId).getPort();
    }
}
