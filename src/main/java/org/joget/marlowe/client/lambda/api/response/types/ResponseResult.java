package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;

/*
* This response is a simple true/false indication.
*/

@Getter
public class ResponseResult extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "result";
    
    //true means added to list of followed contracts, false means already added previously
    @SerializedName("result")
    private boolean addedToFollowedList;
}
