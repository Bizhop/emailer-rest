package fi.bizhop.emailerrest.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import javax.persistence.*;
import java.time.ZonedDateTime;

@Entity
@Table(name = "sent")
@Data
@NoArgsConstructor
public class Sent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NonNull
    @Column(name = "email_from")
    String from;

    @NonNull
    @Column(name = "email_to")
    String to;

    @NonNull
    String subject;

    @Column(columnDefinition = "TEXT")
    String content;

    @NonNull
    ZonedDateTime timestamp;

    public Sent(@NonNull String from, @NonNull String to, @NonNull String subject, String content, @NonNull ZonedDateTime timestamp) {
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.content = content;
        this.timestamp = timestamp;
    }

    public static Sent fromEmail(Email email) {
        return new Sent(email.getFrom(), email.getTo(), email.getSubject(), email.getContent(), email.getTimestamp());
    }
}
