package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;

@Getter
@AllArgsConstructor
public class RequestUnfollow extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "unfollow";
    
    @NonNull private final String contractId;
}
