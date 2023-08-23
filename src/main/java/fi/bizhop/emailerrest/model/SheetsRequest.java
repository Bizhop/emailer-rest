package fi.bizhop.emailerrest.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "sheets_request")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SheetsRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    ZonedDateTime timestamp;

    @Column(nullable = false)
    String name;

    @Column(nullable = false)
    String email;

    String competitionInfo;

    @Column(nullable = false)
    Store store;

    @Column(nullable = false)
    String competitionDate;

    @Column(nullable = false)
    Status status;

    public enum Status {
        REQUESTED, COMPLETED, REJECTED;
    }
}
