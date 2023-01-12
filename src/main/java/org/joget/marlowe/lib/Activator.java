package org.joget.marlowe.lib;

import java.util.ArrayList;
import java.util.Collection;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Activator implements BundleActivator {

    protected Collection<ServiceRegistration> registrationList;

    public void start(BundleContext context) {
        registrationList = new ArrayList<ServiceRegistration>();

        //Register plugin here
        registrationList.add(context.registerService(ContractExecutorTool.class.getName(), new ContractExecutorTool(), null));
//        registrationList.add(context.registerService(ContractInfoDatalistBinder.class.getName(), new ContractInfoDatalistBinder(), null));
//        registrationList.add(context.registerService(ContractDataHashVariable.class.getName(), new ContractDataHashVariable(), null));
    }

    public void stop(BundleContext context) {
        for (ServiceRegistration registration : registrationList) {
            registration.unregister();
        }
    }
}