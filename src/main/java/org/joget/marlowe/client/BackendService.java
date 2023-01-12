package org.joget.marlowe.client;

import org.joget.marlowe.client.lambda.api.request.types.*;
import org.joget.marlowe.client.lambda.api.response.types.*;

public interface BackendService {

    /**
    * Add a contract to the list of Marlowe contracts that the Marlowe Runtime backend is following.
    */
    public ResponseResult followContract(RequestFollow request);
    
    /**
    * Remove a contract to the list of Marlowe contracts that the Marlowe Runtime backend is following.
    */
    public ResponseResult unfollowContract(RequestUnfollow request);
    
    /**
    * Fetch the history of a Marlowe contract.
    */
    public ResponseInfo getContractHistory(RequestGet request);
    
    /**
    * List all Marlowe contract IDs on the blockchain.
    */
    public ResponseContracts listAllContracts(RequestList request);
    
    /**
    * List all Marlowe contract IDs that the Marlowe Runtime backend is following.
    */
    public ResponseContracts listAllFollowedContracts(RequestFollowed request);
    
    /**
    * Build a transaction that applies input to a Marlowe contract.
    */
    public ResponseBody applyInputsToContract(RequestApply request);
    
    /**
    * Build a transaction that creates a new Marlowe contract.
    */
    public ResponseBody createNewContract(RequestCreate request);
    
    /**
    * Submit a signed transaction to the Cardano node.
    */
    public ResponseTxId submitTransaction(RequestSubmit request);
    
    /**
    * Build a transaction that withdraws funds paid by a Marlowe contract.
    */
    public ResponseBody withdrawFundsFromContract(RequestWithdraw request);
    
    /**
    * Wait for the first confirmation of a transaction on the Cardano node.
    */
    public ResponseTxInfo waitTxConfirmation(RequestWait request);
}
