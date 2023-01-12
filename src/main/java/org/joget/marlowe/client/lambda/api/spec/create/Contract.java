package org.joget.marlowe.client.lambda.api.spec.create;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Contract {
    
    @NonNull private String contractId;
    
    @SerializedName("contract")
    private String contractDefinition;
}
