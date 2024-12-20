package com.oneDev.ecommerce.configuration;

import com.xendit.Xendit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XenditCongi {

    @Value("${xendit.api-key}")
    private String xenditApiKey;

    @Bean
    public Xendit xenditClient() {
        Xendit.apiKey = xenditApiKey;
        return new Xendit();
    }
}
