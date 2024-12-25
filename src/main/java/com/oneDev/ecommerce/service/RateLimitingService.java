package com.oneDev.ecommerce.service;

import java.util.function.Supplier;

public interface RateLimitingService {

    <T> T executeWithRateLimit(String key, Supplier<T> operation);
}
