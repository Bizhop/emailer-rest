package fi.bizhop.emailerrest.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "sent")
@Data
public class Sent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
