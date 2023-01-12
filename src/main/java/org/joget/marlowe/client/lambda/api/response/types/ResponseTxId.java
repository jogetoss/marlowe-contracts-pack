package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;

/*
* This response contains a transaction ID.
*/

@Getter
public class ResponseTxId extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "txId";
    
    private String txId;
}
