package com.bashar.oauth2lw.client.jdbc;

import com.bashar.oauth2lw.core.AuthorizationServer;
import com.bashar.oauth2lw.core.ResourceServer;
import com.bashar.oauth2lw.core.SecurityConfig;
import org.h2.server.web.WebServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@SpringBootApplication
public class Oauth2lwClientApplication {
    public static void main(String[] args) {
        SpringApplication.run(Oauth2lwClientApplication.class, args);
    }
}

@Configuration
class customConfig {
    //h2 console config- bean needed to access our H2 database web gui
    @Bean
    ServletRegistrationBean h2servletRegistration() {
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(new WebServlet());
        registrationBean.addUrlMappings("/console/*");
        return registrationBean;
    }
}

@Configuration
class SecurityConfig2 extends SecurityConfig {

    //our users database
    @Autowired
    DataSource dataSource;

    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication().dataSource(dataSource)
                .usersByUsernameQuery("select username, password, activated from user where username=?")
                .authoritiesByUsernameQuery("select username, authority from user_authority where username=?");

        //or you can user your own user details service or any custom beans to load users
//        auth.userDetailsService(new UserDetailsService() {
//            @Override
//            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
//                //user your repositories to load user here by user name
//                return null
//            }
//        });

        //we don't call super here because we want to override the default behaviour from the core lib
    }

    protected void configure(HttpSecurity http) throws Exception {
        // IMPORTANT: order is important here otherwise the super configs will take precedence over our custom rules

        //this is specific to setup the h2 console and allow us to access it without being protected with default login
        //page.
        http.authorizeRequests().antMatchers("/console/**").permitAll()
                .and().csrf().disable().headers().frameOptions().disable();

        //we called super here because we just wanted to extend the security configs without totally override them
        //and to keep the default configured form login from spring security web.
        super.configure(http);
    }
}

@Configuration
class ResourceConfig2 extends ResourceServer {
    //because we are using jdbc token store
    @Autowired
    public TokenStore tokenStore;

    //ovrride this to provide the token store bean to the resource server to know where to validate tokens
    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        resources.tokenStore(tokenStore);
        //we call super here because we don't want to reconfigure the resource id
        super.configure(resources);
    }

    //    @Override
    public void configure(HttpSecurity http) throws Exception {
        //the way these are written is critical, make sure you know how to write these before changing them to your case
        //otherwise you will spend hours trying to figure out why ur rules aren't applied correctly and will cause
        // problems with the general security configs
        http.antMatcher("/protected/**")
                .authorizeRequests().antMatchers("/protected/cats/**").access("#oauth2.hasScope('cats')")
                .and()
                .authorizeRequests().antMatchers("/protected/dogs/**").access("#oauth2.hasScope('dogs')");

        // we don't call super here because these eveyr application should implement its own access rules.
    }
}

@Configuration
class AuthorizationServer2 extends AuthorizationServer {
    //created by springboot automatically since we have info in application.properties
    @Autowired
    DataSource dataSource;

    //define our jdbc token store bean
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints.tokenStore(tokenStore());
        //we called super here to avoid reconfiguring
        super.configure(endpoints);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.jdbc(dataSource)
                .withClient("trusted1").secret("trusted1").scopes("resources").authorizedGrantTypes("authorization_code").redirectUris("http://localhost:9090/redirect")
                .and()
                .withClient("cats-client").secret("cats-client").scopes("cats").authorizedGrantTypes("authorization_code", "password").redirectUris("http://localhostcats/redirect")
                .and()
                .withClient("dogs-client").secret("dogs-client").scopes("dogs").authorizedGrantTypes("authorization_code").redirectUris("http://localhostdogs/redirect");

        //no call for super here because we don't want the core to configure the in memory client for us, we want to override it.

    }
}

/**
 * Refs:
 * 1- https://springframework.guru/using-the-h2-database-console-in-spring-boot-with-spring-security/
 * 2- https://github.com/rajithd/spring-boot-oauth2
 * 3- http://www.mkyong.com/spring-security/spring-security-form-login-using-database/
 */