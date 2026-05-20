package com.bolt.tecnhical.challenger.security;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(SecurityAppProperties.class)
public class SecurityPropertiesConfiguration {
}
