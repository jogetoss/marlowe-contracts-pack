package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;

@Getter
@Builder
public class RequestWait extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "wait";
    
    @NonNull private final String txId;
    
    @Builder.Default
    private int pollingSeconds = 2;
}
