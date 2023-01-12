package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;

@Getter
@NoArgsConstructor
public class RequestFollowed extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "followed";
}
