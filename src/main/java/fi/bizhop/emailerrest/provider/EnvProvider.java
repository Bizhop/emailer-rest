package fi.bizhop.emailerrest.provider;

import org.springframework.stereotype.Component;

@Component
public class EnvProvider {
    public String getenv(String key) {
        return System.getenv(key);
    }
}
