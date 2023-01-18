package org.joget.marlowe.lib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.DefaultHashVariablePlugin;
import org.joget.apps.app.model.EnvironmentVariable;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.form.service.FormUtil;
import org.joget.commons.util.LogUtil;
import org.joget.marlowe.client.BackendService;
import org.joget.marlowe.client.lambda.api.request.types.RequestGet;
import org.joget.marlowe.client.lambda.api.response.types.ResponseInfo;
import org.joget.marlowe.client.lambda.backend.LambdaBackend;
import org.joget.marlowe.service.ContractUtil;
import org.joget.marlowe.service.PluginUtil;

public class ContractDataHashVariable extends DefaultHashVariablePlugin {

    @Override
    public String getName() {
        return "Marlowe Contract Data Hash Variable";
    }

    @Override
    public String getDescription() {
        return "This hash variable plugin allows you to conveniently retrieve various info about Marlowe smart contracts.";
    }
    
    @Override
    public String getPrefix() {
        return "marlowe";
    }

    @Override
    public Collection<String> availableSyntax() {
        Collection<String> syntax = new ArrayList<String>();
        syntax.add("marlowe.history.creation[CONTRACT_ID]");
        syntax.add("marlowe.history.steps[CONTRACT_ID]");
        syntax.add("marlowe.unixTime");
        syntax.add("marlowe.unixTime[TIME_UNIT|AMOUNT]");
        
        return syntax;
    }
    
    @Override
    public String processHashVariable(String variableKey) {
        String paramValue = null;
        
        if (variableKey.contains("[") && variableKey.contains("]")) {
            paramValue = variableKey.substring(variableKey.indexOf("[") + 1, variableKey.indexOf("]"));
            variableKey = variableKey.substring(0, variableKey.indexOf("["));
            
            if (paramValue.isBlank()) {
                LogUtil.debug(getClassName(), "Hash variable parameter cannot be blank.");
                return "";
            }
        }
        
        switch (variableKey) {
            case "history.creation":
            case "history.steps": {
                if (FormUtil.isFormBuilderActive()) {
                    return null;
                }
                
                //Think of easier way of declaring lambda config
                Map<String, String> props = new HashMap();
                AppDefinition appDef = AppUtil.getCurrentAppDefinition();
                Collection<EnvironmentVariable> envVarList = appDef.getEnvironmentVariableList();
                for (EnvironmentVariable o : envVarList) {
                    if ("awsRegion".equalsIgnoreCase(o.getId())) {
                        props.put("awsRegion", o.getValue());
                    }
                    if ("functionName".equalsIgnoreCase(o.getId())) {
                        props.put("functionName", o.getValue());
                    }
                    if ("identityPoolId".equalsIgnoreCase(o.getId())) {
                        props.put("identityPoolId", o.getValue());
                    }
                }
                
                BackendService backendService = new LambdaBackend(props);
                
                ResponseInfo responseInfo = backendService.getContractHistory(new RequestGet(paramValue));
                
                String temp[] = variableKey.split("\\.");
                String historyVariable = temp[1];
                
                Gson gson = new GsonBuilder().create();
                switch (historyVariable) {
                    case "creation":
                        return gson.toJson(responseInfo.getCreation());
                    case "steps":
                        return gson.toJson(responseInfo.getSteps());
                }
                break;
            }
            case "unixTime": {
                if (paramValue == null) {
                    return ContractUtil.getUnixTimeNow().toString();
                }
                
                String temp[] = paramValue.split("\\|");
                String timeUnit = temp[0];
                String timeAmount = temp[1];
                
                ChronoUnit chronoTimeUnit;
                try {
                    chronoTimeUnit = ChronoUnit.valueOf(timeUnit.toUpperCase());
                } catch (Exception e) {
                    LogUtil.warn(getClassName(), "No matching time unit found for '" + timeUnit + "'. Refer to Java 'ChronoUnit' for valid values.");
                    return null;
                }
                
                return ContractUtil.getUnixTime(chronoTimeUnit, Integer.parseInt(timeAmount)).toString();
            }
            default:
                LogUtil.warn(getClassName(), "Unknown variable key '" + variableKey + "' found.");
                return null;
        }
        
        return null;
    }
    
    @Override
    public String escapeHashVariableValue(String value) {
        return AppUtil.escapeHashVariable(value);
    }
    
    //Hash variables don't need any property configuration
    @Override
    public String getPropertyOptions() {
        return "";
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
