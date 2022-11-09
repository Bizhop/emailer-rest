package fi.bizhop.emailerrest;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfiguration implements WebMvcConfigurer {
    @Bean
    public FilterRegistrationBean<AccessFilter> accessFilter() {
        var bean = new FilterRegistrationBean<AccessFilter>();

        bean.setFilter(new AccessFilter());

        bean.addUrlPatterns("/*");

        bean.setOrder(1);

        return bean;
    }
}
