package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;

/*
* This response details the history of a Marlowe contract.
*/

@Getter
public class ResponseInfo extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "info";
    
    private String creation;
    private String[] steps;
}
