package org.joget.marlowe.model;

import lombok.NonNull;

public enum ContractAction {
    
    START_CONTRACT("startContract"),
    EXECUTE_STEP("executeStep"),
    WITHDRAW_FUNDS("withdrawFunds");
    
    private final String value;
    
    ContractAction(final String value) {
        this.value = value;
    }
    
    @Override
    public String toString() {
        return value;
    }
    
    public static ContractAction fromString(@NonNull String text) {
        for (ContractAction action : ContractAction.values()) {
            if ((action.value).equalsIgnoreCase(text)) {
                return action;
            }
        }
        
        return null;
    }
}
