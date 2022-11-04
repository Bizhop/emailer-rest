package fi.bizhop.emailerrest.db;

import fi.bizhop.emailerrest.model.MyStoredCredential;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface TokenRepository extends CrudRepository<MyStoredCredential, String> {
    @NonNull
    List<MyStoredCredential> findAll();
}
