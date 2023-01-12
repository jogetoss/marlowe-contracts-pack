package org.joget.marlowe.client.lambda.api.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class LambdaRequest {
    public String toJsonString() {
        Gson gson = new Gson();
        
        return gson.toJson(this);
    }
    
    public JsonObject toJsonObject() {
        return JsonParser.parseString(this.toJsonString()).getAsJsonObject();
    }
}
