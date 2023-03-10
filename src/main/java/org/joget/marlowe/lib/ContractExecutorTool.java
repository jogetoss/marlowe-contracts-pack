package org.joget.marlowe.lib;

import com.bloxbean.cardano.client.account.Account;
import com.bloxbean.cardano.client.common.ADAConversionUtil;
import com.bloxbean.cardano.client.common.model.Network;
import com.bloxbean.cardano.client.common.model.Networks;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.commons.util.LogUtil;
import org.joget.commons.util.PluginThread;
import org.joget.marlowe.client.BackendService;
import org.joget.marlowe.client.lambda.api.request.types.RequestApply;
import org.joget.marlowe.client.lambda.api.request.types.RequestCreate;
import org.joget.marlowe.client.lambda.api.request.types.RequestFollow;
import org.joget.marlowe.client.lambda.api.request.types.RequestSubmit;
import org.joget.marlowe.client.lambda.api.request.types.RequestWait;
import org.joget.marlowe.client.lambda.api.request.types.RequestWithdraw;
import org.joget.marlowe.client.lambda.api.response.types.ResponseBody;
import org.joget.marlowe.client.lambda.api.response.types.ResponseResult;
import org.joget.marlowe.client.lambda.api.response.types.ResponseTxId;
import org.joget.marlowe.client.lambda.api.response.types.ResponseTxInfo;
import org.joget.marlowe.client.lambda.api.spec.apply.Address;
import org.joget.marlowe.client.lambda.api.spec.apply.InputParty;
import org.joget.marlowe.client.lambda.api.spec.apply.Inputs;
import org.joget.marlowe.client.lambda.api.spec.apply.Role;
import org.joget.marlowe.client.lambda.api.spec.apply.Token;
import org.joget.marlowe.client.lambda.api.spec.submit.Tx;
import org.joget.marlowe.client.lambda.backend.LambdaBackend;
import org.joget.marlowe.model.ContractAction;
import org.joget.marlowe.service.ContractUtil;
import org.joget.marlowe.service.PluginUtil;
import org.joget.plugin.base.DefaultApplicationPlugin;
import org.joget.workflow.model.WorkflowAssignment;
import org.joget.workflow.model.service.WorkflowManager;
import org.joget.workflow.util.WorkflowUtil;
import org.springframework.context.ApplicationContext;

public class ContractExecutorTool extends DefaultApplicationPlugin {
    
    AppService appService;
    AppDefinition appDef;
    WorkflowAssignment wfAssignment;
    WorkflowManager workflowManager;
    
    @Override
    public String getName() {
        return "Marlowe Contract Executor Tool";
    }

    @Override
    public String getDescription() {
        return "This process tool plugin performs various processing to deploy and execute Marlowe smart contracts.";
    }
    
    private void initUtils(Map props) {
        ApplicationContext ac = AppUtil.getApplicationContext();
        
        this.appService = (AppService) ac.getBean("appService");
        this.appDef = (AppDefinition) props.get("appDef");
        this.wfAssignment = (WorkflowAssignment) props.get("workflowAssignment");
        this.workflowManager = (WorkflowManager) ac.getBean("workflowManager");
    }
    
    @Override
    public Object execute(Map props) {
        initUtils(props);
        
        final Network networkType = getNetwork(getPropertyString("networkType"));
        
        final ContractAction contractAction = ContractAction.fromString(getPropertyString("contractAction"));
        
        Account actor;
        
        try {
            BackendService backendService = new LambdaBackend(props);
            ResponseBody responseBody;
                    
            switch (contractAction) {
                case START_CONTRACT: {
                    final String contractCreatorMnemonic = PluginUtil.decrypt(
                            WorkflowUtil.processVariable(getPropertyString("contractCreatorMnemonic"), "", wfAssignment)
                    );
                    actor = new Account(networkType, contractCreatorMnemonic);
                    String actorBaseAddress = actor.baseAddress();
                    
                    /*
                    * FIND A WAY TO DESERIALIZE Marlowe Core JSON to intelligently fill in contract variables prior to creation
                    */
                    final String marloweCoreContractJson = WorkflowUtil.processVariable(getPropertyString("marloweCoreContractJson"), "", wfAssignment);
                    
                    Object[] contractRoles = (Object[]) props.get("contractRoles");
                    Map<String, String> roleMap = new HashMap();
                    if (contractRoles != null && contractRoles.length > 0) {
                        for (Object o : contractRoles) {
                            Map mapping = (HashMap) o;
                            
                            String roleName = WorkflowUtil.processVariable(mapping.get("roleName").toString().trim(), "", wfAssignment);
                            String address = WorkflowUtil.processVariable(mapping.get("address").toString().trim(), "", wfAssignment);
                            
                            roleMap.put(roleName, address);
                        }
                    }
                    
                    responseBody = backendService.createNewContract(
                            RequestCreate.builder()
                                    .contract(marloweCoreContractJson)
//                                    .minUtxo()
                                    .roles(roleMap)
//                                    .metadata()
                                    .change(actorBaseAddress)
//                                    .addresses()
//                                    .collateral()
                                    .build()
                    );
                    break;
                }
                case EXECUTE_STEP: {
                    final String contractId = WorkflowUtil.processVariable(getPropertyString("contractId"), "", wfAssignment);
                    
                    final String roleMnemonic = PluginUtil.decrypt(
                            WorkflowUtil.processVariable(getPropertyString("roleMnemonic"), "", wfAssignment)
                    );
                    actor = new Account(networkType, roleMnemonic);
                    String actorBaseAddress = actor.baseAddress();
                    
                    Object[] contractInputs = (Object[]) props.get("contractInputs");
                    Inputs[] inputsArray = {};
                    if (contractInputs != null && contractInputs.length > 0) {
                        List<Inputs> inputsList = new ArrayList<Inputs>();
                        
                        for (Object o : contractInputs) {
                            Map mapping = (HashMap) o;
                            
                            String inputRoleName = WorkflowUtil.processVariable(mapping.get("inputRoleName").toString().trim(), "", wfAssignment);
                            String currencySymbol = WorkflowUtil.processVariable(mapping.get("currencySymbol").toString().trim(), "", wfAssignment);
                            String tokenName = WorkflowUtil.processVariable(mapping.get("tokenName").toString().trim(), "", wfAssignment);
                            String depositAmount = WorkflowUtil.processVariable(mapping.get("depositAmount").toString().trim(), "", wfAssignment);
                            
                            InputParty inputFromParty;
                            InputParty intoAccount;
                            if (!inputRoleName.isBlank()) {
                                inputFromParty = new Role(inputRoleName);
                                intoAccount = new Role(inputRoleName);
                            } else {
                                inputFromParty = new Address(actorBaseAddress);
                                intoAccount = new Address(actorBaseAddress);
                            }
                            
                            //If detected ADA coin, auto calculate ADA units to lovelaces
                            if (currencySymbol.isBlank() && tokenName.isBlank()) {
                                depositAmount = ADAConversionUtil.adaToLovelace(new BigDecimal(depositAmount)).toString();
                            }
                            
                            inputsList.add(
                                    Inputs.builder()
                                            .inputFromParty(inputFromParty)
                                            .intoAccount(intoAccount)
                                            .ofToken(new Token(currencySymbol, tokenName)) //Defaults to ADA coin if blank
                                            .thatDeposits(new BigInteger(depositAmount))
                                            .build()
                            );
                        }
                        
                        inputsArray = inputsList.toArray(Inputs[]::new);
                    }
                    
                    responseBody = backendService.applyInputsToContract(
                            RequestApply.builder()
                                    .contractId(contractId)
                                    .inputs(inputsArray)
//                                    .validityLowerBound()
//                                    .validityUpperBound()
//                                    .metadata()
                                    .change(actorBaseAddress)
//                                    .addresses()
//                                    .collateral()
                                    .build()
                    );
                    break;
                }
                case WITHDRAW_FUNDS: {
                    final String contractId = WorkflowUtil.processVariable(getPropertyString("contractId"), "", wfAssignment);
                    
                    final String roleMnemonic = PluginUtil.decrypt(
                            WorkflowUtil.processVariable(getPropertyString("roleMnemonic"), "", wfAssignment)
                    );
                    actor = new Account(networkType, roleMnemonic);
                    String actorBaseAddress = actor.baseAddress();
                    
                    final String withdrawRoleName = WorkflowUtil.processVariable(getPropertyString("withdrawRoleName"), "", wfAssignment);
                    
                    responseBody = backendService.withdrawFundsFromContract(
                            RequestWithdraw.builder()
                                    .contractId(contractId)
                                    .role(withdrawRoleName)
//                                    .addresses()
                                    .change(actorBaseAddress)
//                                    .collateral()
                                    .build()
                    );
                    break;
                }
                default:
                    LogUtil.warn(getClassName(), "Unknown contract action found! Aborting plugin execution...");
                    return null;
            }
            
            //Different contract ID & Tx ID calculated somehow VS after submitting to chain. CANNOT TRUST RESPONSEBODY
            if (responseBody == null) {
                LogUtil.warn(getClassName(), "Response body is empty! Aborting plugin execution...");
                storeToWorkflowVariable("FAILED", null, null);
                return null;
            }
            
            if (responseBody.getErrorType() != null) {
                LogUtil.warn(getClassName(), "Response returned error --> " + responseBody.getErrorMessage());
                storeToWorkflowVariable("FAILED", null, null);
                return null;
            }
            
            final Transaction unsignedTx = ContractUtil.deserializeTxCbor(responseBody.getBody().getCborHex());
            final Transaction signedTx = actor.sign(unsignedTx);
            
            final ResponseTxId responseTxId = backendService.submitTransaction(
                    new RequestSubmit(
                            Tx.builder()
                                .cborHex(signedTx.serializeToHex())
                                .build()
                        )
            );
            
            if (responseTxId == null) {
                LogUtil.warn(getClassName(), "Transaction failed to submit...");
                storeToWorkflowVariable("FAILED", null, null);
                return null;
            }
            
            if (responseTxId.getErrorType() != null) {
                LogUtil.warn(getClassName(), "Transaction failed to submit with error --> " + responseTxId.getErrorMessage());
                storeToWorkflowVariable("FAILED", null, null);
                return null;
            }
            
            final String transactionId = responseTxId.getTxId();
            final String contractId = transactionId + "#1";
            
            storeToWorkflowVariable("SUCCESS", transactionId, contractId);
            
            //wait for transaction in separate thread
            Thread waitTxThread = new PluginThread(() -> {
                LogUtil.info(getClassName(), "Waiting for transaction confirmation for --> " + transactionId);

                final ResponseTxInfo responseTxInfo = backendService.waitTxConfirmation(
                        RequestWait.builder()
                                .txId(transactionId)
//                                .pollingSeconds(2) //Defaults to 2 seconds
                                .build()
                );

                LogUtil.info(getClassName(), "Transaction is confirmed for --> " + transactionId);

                if (contractAction.equals(ContractAction.START_CONTRACT)) {
                    //if this is create contract, auto follow the contract
                    if (responseTxInfo != null && responseTxInfo.getTransaction() != null) {
                        final ResponseResult responseResult = backendService.followContract(
                                new RequestFollow(contractId)
                        );
                        LogUtil.info(
                                getClassName(), 
                                responseResult.isAddedToFollowedList() ? "Contract " + contractId + " has been added to follow list." 
                                        : "Contract " + contractId + " already followed previously."
                        );
                    } else {
                        LogUtil.warn(getClassName(), "Unexpected error waiting for transaction confirmation...");
                    }
                }
            });
            waitTxThread.start();
            
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error executing " + getName() + "...");
        }
        
        return null;
    }
    
    private void storeToWorkflowVariable(String txSubmitStatus, String txId, String contractId) {
        String wfTransactionSubmitStatus = getPropertyString("wfTransactionSubmitStatus");
        String wfTransactionId = getPropertyString("wfTransactionId");
        String wfContractId = getPropertyString("wfContractId");
        
        storeValuetoActivityVar(
                wfTransactionSubmitStatus, 
                txSubmitStatus
        );
        storeValuetoActivityVar(
                wfTransactionId, 
                txId
        );
        storeValuetoActivityVar(
                wfContractId, 
                contractId
        );
    }
    
    private void storeValuetoActivityVar(String variable, String value) {
        if (variable.isEmpty() || value == null) {
            return;
        }
        
        workflowManager.activityVariable(wfAssignment.getActivityId(), variable, value);
    }
    
    private Network getNetwork(String networkType) {
        switch (networkType) {
            case "mainnet":
                return Networks.mainnet();
            case "previewTestnet":
                return Networks.preview();
            case "preprodTestnet":
                return Networks.preprod();
            default:
                LogUtil.warn(getClassName(), "Unknown network type found!");
                return null;
        }
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ContractExecutorTool.json", null, true, PluginUtil.MESSAGE_PATH);
    }
    
    @Override
    public String getVersion() {
        return PluginUtil.getProjectVersion(this.getClass());
    }
    
    @Override
    public String getLabel() {
        return getName();
    }

    @Override
    public String getClassName() {
        return getClass().getName();
    }
}
