package org.joget.marlowe.client.lambda.api.response;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;

@Getter
public abstract class LambdaResponse {
    
    protected String errorType;
    protected String errorMessage;
    
    public String toJsonString() {
        Gson gson = new Gson();
        
        return gson.toJson(this);
    }
    
    public JsonObject toJsonObject() {
        return JsonParser.parseString(this.toJsonString()).getAsJsonObject();
    }
}
