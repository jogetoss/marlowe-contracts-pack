package org.joget.marlowe.service;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.Special;
import com.bloxbean.cardano.client.transaction.spec.ListPlutusData;
import com.bloxbean.cardano.client.transaction.spec.Redeemer;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import com.bloxbean.cardano.client.util.HexUtil;
import java.math.BigInteger;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.joget.commons.util.LogUtil;

/**
 * Utility class for processing Marlowe contracts
 */
public class ContractUtil {
    
    private ContractUtil() {}
    
    /**
     * Reference: https://input-output-hk.github.io/cardano-node/cardano-api/lib/Cardano-Api-TxBody.html
     * 
     * @param txBodyCborHex
     * @return Transaction object
     */
    public static Transaction deserializeTxBodyCborToTx(String txBodyCborHex) {
        try {
            byte[] cborBytes = HexUtil.decodeHexString(txBodyCborHex);
            
            List<DataItem> dataItemList = CborDecoder.decode(cborBytes);
            Array array = (Array) dataItemList.get(0);
            List<DataItem> txBodyItemList = array.getDataItems();

            DataItem txnBodyDI = txBodyItemList.get(0); //TxBody (Map)
//            DataItem scriptDI = txBodyItemList.get(1); //Script (Array)
            DataItem txBodyScriptDataDI = txBodyItemList.get(2); //TxBodyScriptData (Array)
            DataItem redeemersArrayDI = txBodyItemList.get(3); //AuxiliaryData (Array) {Redeemers?}
            DataItem txScriptValidityDI = txBodyItemList.get(4); //TxScriptValidity (SimpleValue)
//            DataItem byteStringDI = txBodyItemList.get(5); //TxBody ByteString (SimpleValue)

            boolean checkAuxData = true;
            boolean isValid = true;
            //If it's special it can be either a bool or null. If it's null, then it's empty auxiliary data, otherwise
            //not a valid encoding
            if (txScriptValidityDI != null && txScriptValidityDI instanceof Special) {
                if (txScriptValidityDI == SimpleValue.FALSE) {
                    isValid = false;
                } else if (txScriptValidityDI == SimpleValue.NULL) {
                    checkAuxData = false;
                }
            }
            
//            AuxiliaryData auxiliaryData = null;
            
            List<Redeemer> redeemers = new ArrayList<>();
            if (checkAuxData && redeemersArrayDI != null && MajorType.ARRAY.equals(redeemersArrayDI.getMajorType())) {
                List<DataItem> redeemerDIList = ((Array) redeemersArrayDI).getDataItems();
                redeemers = new ArrayList<>();

                for(DataItem redeemerDI: redeemerDIList) {
                    if (redeemerDI == SimpleValue.BREAK) continue;
                    redeemers.add(Redeemer.deserialize((Array) redeemerDI));
                }
            }
            
            return Transaction.builder()
                    .body(TransactionBody.deserialize((Map) txnBodyDI))
                    .witnessSet(
                            TransactionWitnessSet.builder()
                                    .redeemers(redeemers)
                                    .plutusDataList(
                                            ListPlutusData.deserialize((Array) txBodyScriptDataDI).getPlutusDataList()
                                    )
                                    .build()
                    )
                    .isValid(isValid)
//                    .auxiliaryData(auxiliaryData)
                    .build();
        } catch (Exception e) {
            LogUtil.error(ContractUtil.class.getName(), e, "Unable to deserialize txBodyCborHex to Transaction");
        }
        
        return null;
    }
    
    public static BigInteger getUnixTimeNow() {
        return getUnixTimeAhead(ChronoUnit.SECONDS, 0);
    }
    
    public static BigInteger getUnixTimeAhead(ChronoUnit timeUnit, long timeAhead) {
        return BigInteger.valueOf(Instant.now().plus(timeAhead, timeUnit).toEpochMilli());
    }
}
