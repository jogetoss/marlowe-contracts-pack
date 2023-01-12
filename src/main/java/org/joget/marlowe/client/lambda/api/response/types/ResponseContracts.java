package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;

/*
* This response is a list of contract IDs.
*/

@Getter
public class ResponseContracts extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "contracts";
    
    private String[] contractIds;
}
