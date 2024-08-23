package fi.bizhop.emailerrest;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.introspection.OAuth2IntrospectionException;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    private OpaqueTokenIntrospector introspector;
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        if(introspector == null) introspector = new GoogleTokenIntrospector();

        http
            .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .anyRequest().authenticated()
                .and().cors()
                .and().csrf().disable()
                .oauth2ResourceServer()
                .opaqueToken()
                .introspector(introspector);

//        http.cors().and().csrf().disable();
    }

    public static class GoogleTokenIntrospector implements OpaqueTokenIntrospector {
        private final RestTemplate restTemplate = new RestTemplate();

        @Override
        public OAuth2AuthenticatedPrincipal introspect(String token) {
            try {
                var url = "https://oauth2.googleapis.com/tokeninfo?access_token=" + token;
                Map<String, Object> response  = restTemplate.getForObject(url, Map.class);

                if(response == null || response.get("error") != null) {
                    throw new OAuth2IntrospectionException("Invalid token");
                }

                // Convert date fields to Instant
                if (response.containsKey("exp")) {
                    response.put("exp", Instant.ofEpochSecond(Long.parseLong(response.get("exp").toString())));
                }
                if (response.containsKey("iat")) {
                    response.put("iat", Instant.ofEpochSecond(Long.parseLong(response.get("iat").toString())));
                }

                return new DefaultOAuth2AuthenticatedPrincipal(
                        response.get("sub").toString(),
                        response,
                        Collections.emptyList()
                );
            } catch (RestClientException e) {
                throw new OAuth2IntrospectionException("Invalid token", e);
            }
        }
    }
}
