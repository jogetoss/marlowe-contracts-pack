package org.joget.marlowe.client.lambda.api.spec.apply;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Role implements InputParty {
    
    @SerializedName("role_token")
    @NonNull private final String roleName;
    
    public Role(String roleName) {
        this.roleName = roleName;
    }
}
