package org.joget.marlowe.client.lambda.api.request.types;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;
import org.joget.marlowe.service.ContractUtil;

@Getter
@Builder
public class RequestCreate extends LambdaRequest {
    
    @SerializedName("request")
    private final String requestName = "create";
    
    @NonNull private JsonObject contract;
    
    @Builder.Default
    private int minUtxo = 3000000; //3 ADA
    
    @Builder.Default
    private Map<String, String> roles = Map.of();
    
    @Builder.Default
    private JsonObject metadata = new JsonObject();
    
    @Builder.Default
    private String[] addresses = new String[] {};
    
    @NonNull private String change;
    
    @Builder.Default
    private String[] collateral = new String[] {}; //Input(s) automatically selected by Marlowe
    
    public static class RequestCreateBuilder {
        public RequestCreateBuilder contract(String marloweCoreContractJson) {
            this.contract = JsonParser.parseString(marloweCoreContractJson).getAsJsonObject();
            return this;
        }
        
        /**
         * Roles is a {@link Map} collection that contains "role name" as {@link String} that belongs to a "bech32 address" as {@link String}.
         * <br>
         * <p>
         * <b>Note</b>: Bech32 addresses are auto converted to Base16 address format to be consumed by backend.
         * </p>
         */
        public RequestCreateBuilder roles(Map<String, String> rolesMap) {
            for (Map.Entry<String, String> entry : rolesMap.entrySet()) {
                entry.setValue(
                        ContractUtil.bech32AddressToBase16(entry.getValue())
                );
            }
            
            this.roles$value = rolesMap;
            this.roles$set = true;
            
            return this;
        }
    }
}
