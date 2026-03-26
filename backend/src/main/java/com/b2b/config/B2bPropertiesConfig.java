package com.b2b.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({B2bJwtProperties.class, B2bMallSmsProperties.class})
public class B2bPropertiesConfig {}
