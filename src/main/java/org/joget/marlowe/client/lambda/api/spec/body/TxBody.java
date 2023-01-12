package org.joget.marlowe.client.lambda.api.spec.body;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class TxBody {
    
    @Builder.Default
    @NonNull private String type = "";
    
    @Builder.Default
    private final String description = "";
    
    @NonNull private final String cborHex; 
}
