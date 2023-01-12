package org.joget.marlowe.client.lambda.api.spec.apply;

import com.google.gson.annotations.SerializedName;
import java.math.BigInteger;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class Inputs {
    
    @SerializedName("input_from_party")
    @NonNull private final InputParty inputFromParty;
    
    @SerializedName("into_account")
    @NonNull private final InputParty intoAccount;
    
    @SerializedName("of_token")
    @Builder.Default
    @NonNull private Token ofToken = Token.ADA;
    
    @SerializedName("that_deposits")
    @NonNull private final BigInteger thatDeposits;
}
