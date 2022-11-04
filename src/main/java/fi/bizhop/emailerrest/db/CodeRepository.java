package fi.bizhop.emailerrest.db;

import fi.bizhop.emailerrest.model.Code;
import fi.bizhop.emailerrest.model.Store;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CodeRepository extends CrudRepository<Code, Long> {
    @NonNull
    List<Code> findAll();

    List<Code> findAllByUsed(boolean used);

    List<Code> findAllByStoreAndUsedFalse(Store store);
}
