package fi.bizhop.emailerrest;

import fi.bizhop.emailerrest.model.Code;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.util.List;

public interface CodeRepository extends CrudRepository<Code, Long> {
    @NonNull
    List<Code> findAll();
}
