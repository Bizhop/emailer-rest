package fi.bizhop.emailerrest.db;

import fi.bizhop.emailerrest.model.SheetsRequest;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SheetsRequestRepository extends CrudRepository<SheetsRequest, Long> {
    @NonNull
    List<SheetsRequest> findAll();
}
