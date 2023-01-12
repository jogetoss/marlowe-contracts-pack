package org.joget.marlowe.client.lambda.api.response.types;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import org.joget.marlowe.client.lambda.api.response.LambdaResponse;
import org.joget.marlowe.client.lambda.api.spec.body.TxBody;

/*
* This response contains a serialized transaction body.
*/

@Getter
public class ResponseBody extends LambdaResponse {
    
    @SerializedName("response")
    private final String responseName = "body";
    
    private String txId;
    private String contractId;
    private TxBody body;
}
