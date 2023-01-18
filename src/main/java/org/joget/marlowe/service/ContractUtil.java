package org.joget.marlowe.service;

import co.nstant.in.cbor.CborDecoder;
import co.nstant.in.cbor.model.Array;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.DataItem;
import co.nstant.in.cbor.model.MajorType;
import co.nstant.in.cbor.model.Map;
import co.nstant.in.cbor.model.SimpleValue;
import co.nstant.in.cbor.model.Special;
import com.bloxbean.cardano.client.crypto.Bech32;
import com.bloxbean.cardano.client.transaction.spec.AuxiliaryData;
import com.bloxbean.cardano.client.transaction.spec.ListPlutusData;
import com.bloxbean.cardano.client.transaction.spec.PlutusData;
import com.bloxbean.cardano.client.transaction.spec.PlutusV2Script;
import com.bloxbean.cardano.client.transaction.spec.Redeemer;
import com.bloxbean.cardano.client.transaction.spec.Transaction;
import com.bloxbean.cardano.client.transaction.spec.TransactionBody;
import com.bloxbean.cardano.client.transaction.spec.TransactionWitnessSet;
import com.bloxbean.cardano.client.util.HexUtil;
import java.math.BigInteger;
import java.nio.ByteBuffer;
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
     * <p>
     * References: 
     * <br>
     * - https://input-output-hk.github.io/cardano-node/cardano-api/lib/Cardano-Api-TxBody.html
     * <br>
     * - https://github.com/input-output-hk/cardano-ledger/blob/master/eras/babbage/test-suite/cddl-files/babbage.cddl
     * <br>
     * - https://input-output-hk.github.io/cardano-node/cardano-api/lib/src/Cardano.Api.Tx.html
     * <br>
     * - https://input-output-hk.github.io/cardano-node/cardano-api/lib/src/Cardano.Api.Script.html#AnyScriptLanguage
     * </p>
     * <br>
     * <b>Note:</b> This follows the CBOR format returned by {@link ResponseBody} cborHex. 
     * Depending on future changes to marlowe runtime, this may become useless since you can simply do {@link Transaction#deserialize(byte[])}
     * 
     * @param txCborHex
     * @return {@link Transaction} object
     */
    public static Transaction deserializeTxCbor(String txCborHex) {
        try {
            byte[] cborBytes = HexUtil.decodeHexString(txCborHex);
            
            List<DataItem> dataItemList = CborDecoder.decode(cborBytes);
            Array array = (Array) dataItemList.get(0);
            List<DataItem> txBodyItemList = array.getDataItems();

            DataItem txnBodyDI = txBodyItemList.get(0); //TxBody (Map)
            DataItem plutusV2ScriptsDI = txBodyItemList.get(1); //PlutusV2Script (Array) [See: AnyScriptLanguage]
            DataItem plutusDataDI = txBodyItemList.get(2); //PlutusData (Array)
            DataItem redeemersDI = txBodyItemList.get(3); //Redeemers (Array)
            DataItem txValidityDI = txBodyItemList.get(4); //TxValidity (boolean)
            DataItem auxiliaryDataDI = txBodyItemList.get(5); //AuxiliaryData (Map) OR null
            
            List<PlutusData> plutusDataList = null;
            if (plutusDataDI != null && MajorType.ARRAY.equals(plutusDataDI.getMajorType())) {
                Array plutusDataArray = (Array) plutusDataDI;
                
                plutusDataList = ListPlutusData.deserialize(plutusDataArray).getPlutusDataList();
            }
            
            List<Redeemer> redeemers = null;
            if (redeemersDI != null && MajorType.ARRAY.equals(redeemersDI.getMajorType())) {
                redeemers = new ArrayList<>();
                
                List<DataItem> redeemerDIList = ((Array) redeemersDI).getDataItems();

                for (DataItem redeemerDI: redeemerDIList) {
                    if (redeemerDI == SimpleValue.BREAK) { continue; }
                    
                    redeemers.add(Redeemer.deserialize((Array) redeemerDI));
                }
            }
            
            List<PlutusV2Script> plutusV2Scripts = null;
            if (plutusV2ScriptsDI != null && MajorType.ARRAY.equals(plutusV2ScriptsDI.getMajorType())) {
                plutusV2Scripts = new ArrayList<>();
                
                List<DataItem> plutusV2ScriptsDIList = ((Array) plutusV2ScriptsDI).getDataItems();
                
                for (DataItem plutusV2ScriptDI: plutusV2ScriptsDIList) {
                    if (!MajorType.ARRAY.equals(plutusV2ScriptDI.getMajorType())) { continue; }
                    
                    for (DataItem plutusScriptDI: ((Array) plutusV2ScriptDI).getDataItems()) {
                        if (!MajorType.BYTE_STRING.equals(plutusScriptDI.getMajorType())) { continue; }
                        
                        byte[] plutusByteWrapper = ((ByteString) plutusScriptDI).getBytes();

                        byte[] finalBytes = ByteBuffer.allocate(plutusByteWrapper.length)
                                .put(plutusByteWrapper)
                                .array();

                        plutusV2Scripts.add(PlutusV2Script.deserialize(new ByteString(finalBytes)));
                    }
                    
                }
            }
            
            boolean checkAuxData = true;
            boolean isValid = true;
            //If it's special it can be either a bool or null. If it's null, then it's empty auxiliary data, otherwise
            //not a valid encoding
            if (txValidityDI != null && txValidityDI instanceof Special) {
                if (txValidityDI == SimpleValue.FALSE) {
                    isValid = false;
                } else if (txValidityDI == SimpleValue.NULL) {
                    checkAuxData = false;
                }
            }
            
            AuxiliaryData auxiliaryData = null;
            if (checkAuxData && auxiliaryDataDI != null && MajorType.MAP.equals(auxiliaryDataDI.getMajorType())) {
                auxiliaryData = AuxiliaryData.deserialize((Map) auxiliaryDataDI);
            }
            
            return Transaction.builder()
                    .body(TransactionBody.deserialize((Map) txnBodyDI)) //This certainly exists, no need checking for deserialization
                    .witnessSet(
                            TransactionWitnessSet.builder()
                                    .plutusDataList(plutusDataList)
                                    .redeemers(redeemers)
                                    .plutusV2Scripts(plutusV2Scripts)
                                    .build()
                    )
                    .isValid(isValid)
                    .auxiliaryData(auxiliaryData)
                    .build();
        } catch (Exception e) {
            LogUtil.error(ContractUtil.class.getName(), e, "Unable to deserialize transaction CBOR");
        }
        
        return null;
    }
    
    public static String bech32AddressToBase16(String bech32Address) {
        return HexUtil.encodeHexString(
                Bech32.decode(bech32Address).data
        );
    }
    
    public static BigInteger getUnixTimeNow() {
        return getUnixTime(ChronoUnit.SECONDS, 0);
    }
    
    public static BigInteger getUnixTime(ChronoUnit timeUnit, long timeAmount) {
        return BigInteger.valueOf(Instant.now().plus(timeAmount, timeUnit).toEpochMilli());
    }
}
