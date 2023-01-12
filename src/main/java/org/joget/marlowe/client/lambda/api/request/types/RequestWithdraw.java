package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;

@Getter
@Builder
public class RequestWithdraw extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "withdraw";
    
    @NonNull private String contractId;
    
    @NonNull private String role; //role name
    
    @Builder.Default
    private String[] addresses = new String[] {};
    
    @NonNull private String change;
    
    @Builder.Default
    private String[] collateral = new String[] {}; //Input(s) automatically selected by Marlowe
}
