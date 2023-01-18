package org.joget.marlowe.client.lambda.backend;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import java.util.Map;
import lombok.NonNull;
import org.joget.commons.util.LogUtil;
import org.joget.marlowe.client.BackendService;
import org.joget.marlowe.client.lambda.api.request.LambdaRequest;
import org.joget.marlowe.client.lambda.api.request.types.*;
import org.joget.marlowe.client.lambda.api.response.types.*;
import org.joget.marlowe.client.lambda.service.AwsUtil;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentity.CognitoIdentityClient;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetCredentialsForIdentityResponse;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdRequest;
import software.amazon.awssdk.services.cognitoidentity.model.GetIdResponse;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.LambdaException;

public class LambdaBackend implements BackendService {
    
    private final Gson gson;
    
    private final Region awsRegion;
    private final String identityPoolId;
    private final String functionName;
    
    private LambdaClient lambdaClient = null;
    
    public LambdaBackend(@NonNull Map props) {
        this.gson = new Gson();
        
        this.awsRegion = AwsUtil.getAwsRegion((String) props.get("awsRegion"));
        this.identityPoolId = (String) props.get("identityPoolId");
        this.functionName = (String) props.get("functionName");
 
        try {
            this.lambdaClient = LambdaClient.builder()
                    .region(awsRegion)
                    .credentialsProvider(getCredentials(awsRegion, identityPoolId))
                    .build();
        } catch (LambdaException e) {
            LogUtil.error(getClassName(), e, "Unable to initialize AWS Lambda client.");
            closeLambdaClient();
        }
    }
    
    @Override
    public ResponseResult followContract(RequestFollow request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseResult.class);
    }

    @Override
    public ResponseResult unfollowContract(RequestUnfollow request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseResult.class);
    }

    @Override
    public ResponseInfo getContractHistory(RequestGet request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseInfo.class);
    }

    @Override
    public ResponseContracts listAllContracts(RequestList request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseContracts.class);
    }

    @Override
    public ResponseContracts listAllFollowedContracts(RequestFollowed request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseContracts.class);
    }

    @Override
    public ResponseBody applyInputsToContract(RequestApply request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseBody.class);
    }

    @Override
    public ResponseBody createNewContract(RequestCreate request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseBody.class);
    }

    @Override
    public ResponseTxId submitTransaction(RequestSubmit request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseTxId.class);
    }

    @Override
    public ResponseBody withdrawFundsFromContract(RequestWithdraw request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseBody.class);
    }
    
    @Override
    public ResponseTxInfo waitTxConfirmation(RequestWait request) {
        return gson.fromJson(invokeLambdaFunction(request), ResponseTxInfo.class);
    }
    
    private String invokeLambdaFunction(@NonNull LambdaRequest request) {
        if (functionName.isBlank()) {
            LogUtil.warn(getClassName(), "Function name cannot be blank for AWS Lambda invocation...");
            return null;
        }
        
        try {
            final InvokeResponse response = lambdaClient.invoke(
                    InvokeRequest.builder()
                            .functionName(functionName)
                            .payload(
                                    SdkBytes.fromUtf8String(request.toJsonString())
                            )
                            .build()
            );
            
            final String responseJson = response.payload().asUtf8String();
            
            //Correct JSON response should be an object. Any errors returned by cardano is a plain String.
            try {
                JsonParser.parseString(responseJson).getAsJsonObject();
            } catch (Exception e) {
                LogUtil.warn(getClassName(), "Backend returned error --> " + responseJson);
                closeLambdaClient();
                return null;
            }
            
            return responseJson;
        } catch (Exception e) {
            LogUtil.error(getClassName(), e, "Error invoking lambda function --> " + functionName + " . Reason: " + e.getMessage());
            closeLambdaClient();
        }
        
        return null;
    }
    
    public void forceCloseClient() {
        closeLambdaClient();
    }
    
    private void closeLambdaClient() {
        if (lambdaClient != null) {
            lambdaClient.close();
        }
    }
    
    //TO-DO: FUTURE - Allow more ways to authenticate with lambda client
    private static AwsCredentialsProvider getCredentials(@NonNull Region region, @NonNull String identityPoolId) {
        CognitoIdentityClient cognitoClient = CognitoIdentityClient.builder()
                .region(region)
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();
        
        GetIdResponse getIdResponse = cognitoClient.getId(
                GetIdRequest.builder()
                        .identityPoolId(identityPoolId)
                        .build()
        );
        
        GetCredentialsForIdentityResponse response = cognitoClient.getCredentialsForIdentity(
                GetCredentialsForIdentityRequest.builder()
                        .identityId(getIdResponse.identityId())
                        .build()
        );
        
        AwsSessionCredentials awsSessionCreds = AwsSessionCredentials.create(
                response.credentials().accessKeyId(), 
                response.credentials().secretKey(), 
                response.credentials().sessionToken()
        );
        
        return StaticCredentialsProvider.create(awsSessionCreds);
    }
    
    private static String getClassName() {
        return LambdaBackend.class.getName();
    }
}
