package fi.bizhop.emailerrest.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;

@Entity
@Table(name = "code")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Code {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull Store store;
    @NonNull String code;
    @NonNull String valid;
    boolean used;
}
