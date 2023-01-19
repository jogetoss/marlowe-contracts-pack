package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;

/*
* This response contains full information about a transaction.
*/

@Getter
public class ResponseTxInfo extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "txInfo";
    
    private JsonObject transaction;
}
