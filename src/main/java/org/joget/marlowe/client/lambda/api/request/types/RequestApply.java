package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;
import org.joget.marlowe.client.lambda.api.spec.apply.Inputs;
import org.joget.marlowe.service.ContractUtil;

@Getter
@Builder
public class RequestApply extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "apply";
    
    @NonNull private String contractId;
    
    @NonNull private Inputs[] inputs;
    
    @Builder.Default
    @NonNull private BigInteger validityLowerBound = ContractUtil.getUnixTime(ChronoUnit.MINUTES, -2);
    
    @Builder.Default
    @NonNull private BigInteger validityUpperBound = ContractUtil.getUnixTime(ChronoUnit.MINUTES, 2);
    
    @Builder.Default
    private JsonObject metadata = new JsonObject();
    
    @Builder.Default
    private String[] addresses = new String[] {};
    
    @NonNull private String change;
    
    @Builder.Default
    private String[] collateral = new String[] {}; //Input(s) automatically selected by Marlowe
}
