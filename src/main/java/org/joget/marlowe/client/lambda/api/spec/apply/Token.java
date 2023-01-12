package org.joget.marlowe.client.lambda.api.spec.apply;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class Token {
    
    /**
     * ADA Token
     */
    public transient static final Token ADA = new Token("","");
    
    /**
     * PLUTUS --> Policy ID
     */
    @SerializedName("currency_symbol")
    @NonNull private final String currencySymbol;
    
    /**
     * PLUTUS --> Asset Name
     */
    @SerializedName("token_name")
    @NonNull private final String tokenName;
    
    public Token(String currencySymbol, String tokenName) {        
        this.currencySymbol = currencySymbol;
        this.tokenName = tokenName;
    }
}
