package pths.server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private String name;
    private String password;

    public String getCredentials() {
        String cred = name + ":" + password;
        return Base64.getEncoder().encodeToString(cred.getBytes());
    }
}
