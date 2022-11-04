package fi.bizhop.emailerrest;

import com.google.api.client.auth.oauth2.StoredCredential;
import com.google.api.client.util.store.DataStore;
import com.google.api.client.util.store.DataStoreFactory;
import fi.bizhop.emailerrest.db.TokenRepository;
import fi.bizhop.emailerrest.model.MyStoredCredential;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class TokenDataStoreFactory implements DataStoreFactory {
    final TokenRepository tokenRepository;

    @Override
    public DataStore<StoredCredential> getDataStore(String id) {
        return new TokenDataStore(this, id, tokenRepository);
    }

    @RequiredArgsConstructor
    static class TokenDataStore implements DataStore<StoredCredential> {
        final DataStoreFactory dataStoreFactory;
        final String id;
        final TokenRepository tokenRepository;

        @Override
        public DataStoreFactory getDataStoreFactory() {
            return this.dataStoreFactory;
        }

        @Override
        public String getId() {
            return this.id;
        }

        @Override
        public int size() {
            return tokenRepository.findAll().size();
        }

        @Override
        public boolean isEmpty() {
            return tokenRepository.findAll().size() == 0;
        }

        @Override
        public boolean containsKey(String key) {
            return tokenRepository.existsById(key);
        }

        @Override
        public boolean containsValue(StoredCredential value) {
            return tokenRepository.findAll().stream()
                    .anyMatch(token -> value.equals(token.getStoredCredential()));
        }

        @Override
        public Set<String> keySet() {
            return tokenRepository.findAll().stream()
                    .map(MyStoredCredential::getId)
                    .collect(Collectors.toSet());
        }

        @Override
        public Collection<StoredCredential> values() {
            return tokenRepository.findAll().stream()
                    .map(MyStoredCredential::getStoredCredential)
                    .toList();
        }

        @Override
        public StoredCredential get(String key) {
            return tokenRepository.findById(key)
                    .map(MyStoredCredential::getStoredCredential)
                    .orElse(null);
        }

        @Override
        public DataStore<StoredCredential> set(String key, StoredCredential value) {
            var entity = new MyStoredCredential(key, value);
            tokenRepository.save(entity);
            return this;
        }

        @Override
        public DataStore<StoredCredential> clear() {
            tokenRepository.deleteAll();
            return this;
        }

        @Override
        public DataStore<StoredCredential> delete(String key) {
            tokenRepository.deleteById(key);
            return this;
        }
    }
}
