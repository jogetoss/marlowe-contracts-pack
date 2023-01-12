package org.joget.marlowe.client.lambda.api.spec.apply;

import lombok.Getter;
import lombok.NonNull;

@Getter
public class Address implements InputParty {
    
    @NonNull private final String address;
    
    public Address(String address) {
        this.address = address;
    }
}
