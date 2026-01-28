package org.openpickles.policy.engine.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Forward specific frontend routes to index.html
        registry.addViewController("/login").setViewName("forward:/index.html");
        registry.addViewController("/dashboard").setViewName("forward:/index.html");
        registry.addViewController("/policies").setViewName("forward:/index.html");
        registry.addViewController("/policies/**").setViewName("forward:/index.html");
        registry.addViewController("/policy-bundles").setViewName("forward:/index.html");
        registry.addViewController("/policy-bindings").setViewName("forward:/index.html");
        registry.addViewController("/services").setViewName("forward:/index.html");
        registry.addViewController("/services/**").setViewName("forward:/index.html");
        registry.addViewController("/entitlements").setViewName("forward:/index.html");
        registry.addViewController("/users").setViewName("forward:/index.html");
        registry.addViewController("/users/**").setViewName("forward:/index.html");
    }
}
