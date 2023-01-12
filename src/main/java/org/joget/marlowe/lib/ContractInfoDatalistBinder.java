package org.joget.marlowe.lib;

import java.util.Map;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.datalist.model.DataList;
import org.joget.apps.datalist.model.DataListBinderDefault;
import org.joget.apps.datalist.model.DataListCollection;
import org.joget.apps.datalist.model.DataListColumn;
import org.joget.apps.datalist.model.DataListFilterQueryObject;
import org.joget.marlowe.service.PluginUtil;

public class ContractInfoDatalistBinder extends DataListBinderDefault {

    @Override
    public String getName() {
        return "Marlowe Contract Info Datalist Binder";
    }

    @Override
    public String getDescription() {
        return "This datalist binder plugin loads various contract related data directly from blockchain.";
    }

    @Override
    public DataListColumn[] getColumns() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getPrimaryKeyColumnName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DataListCollection getData(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects, String sort, Boolean desc, Integer start, Integer rows) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getDataTotalRowCount(DataList dataList, Map properties, DataListFilterQueryObject[] filterQueryObjects) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ContractInfoDatalistBinder.json", null, true, PluginUtil.MESSAGE_PATH);
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
