package com.bashar.oauth2lw.client;

import com.bashar.oauth2lw.core.AuthorizationServer;
import com.bashar.oauth2lw.core.ResourceServer;
import com.bashar.oauth2lw.core.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;

/**
 * Created by Bashar on 2017-01-08.
 */

@SpringBootApplication
@Import({ResourceServer.class, SecurityConfig.class})
public class App {
    public static void main(String[] args) {
        ConfigurableApplicationContext run = SpringApplication.run(App.class, args);
    }
}

@Configuration
class AuthorizationServer2 extends AuthorizationServer {
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient("trusted1").secret("trusted1").scopes("resources")
                .authorizedGrantTypes("authorization_code").redirectUris("http://localhost:9090/redirect");
    }
}