package org.joget.marlowe.client.lambda.service;

import lombok.NonNull;
import software.amazon.awssdk.regions.Region;

public class AwsUtil {
    
    private AwsUtil() {}
    
    public static Region getAwsRegion(@NonNull String regionId) {
        return Region.regions().stream()
                .filter(reg -> regionId.equals(reg.id()))
                .findAny()
                .orElse(null);
    }
    
    public static String[] getAwsRegionIds() {
        return Region.regions().stream().map(Region::id).toArray(String[]::new);
    }
}
