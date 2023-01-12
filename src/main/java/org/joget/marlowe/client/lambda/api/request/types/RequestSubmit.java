package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;
import org.joget.marlowe.client.lambda.api.spec.submit.Tx;

@Getter
@AllArgsConstructor
public class RequestSubmit extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "submit";
    
    @NonNull private final Tx tx;
}
