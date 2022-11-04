package fi.bizhop.emailerrest.model;

import com.google.api.client.auth.oauth2.StoredCredential;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "stored_credential")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyStoredCredential {
    @Id
    private String id;

    private StoredCredential storedCredential;
}
