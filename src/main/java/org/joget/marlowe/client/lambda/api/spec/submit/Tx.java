package org.joget.marlowe.client.lambda.api.spec.submit;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Tx {
    
    @Builder.Default
    @NonNull private String type = "Tx BabbageEra";
    
    @Builder.Default
    private final String description = "";
    
    @NonNull private final String cborHex;
    
}
