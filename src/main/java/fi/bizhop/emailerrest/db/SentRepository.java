package fi.bizhop.emailerrest.db;

import fi.bizhop.emailerrest.model.Sent;
import org.springframework.data.repository.CrudRepository;
import org.springframework.lang.NonNull;

import java.time.ZonedDateTime;
import java.util.List;

public interface SentRepository extends CrudRepository<Sent, Long> {
    @NonNull
    List<Sent> findAll();

    List<Sent> findAllByTimestampBetween(ZonedDateTime from, ZonedDateTime to);
}
