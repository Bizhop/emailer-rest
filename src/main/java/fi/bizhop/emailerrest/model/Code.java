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
    @NonNull
    @Column(nullable = false)
    Store store;
    @NonNull
    @Column(unique = true, nullable = false)
    String code;
    @NonNull
    @Column(nullable = false)
    String valid;
    boolean used;
}
