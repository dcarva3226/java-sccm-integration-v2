package com.organization.live.survey.reclamation.ui.component.scene1;

import java.util.List;

import static com.organization.live.survey.reclamation.ui.component.ReclamationPlanComponent.*;
import com.organization.live.survey.reclamation.db.Db;
import com.organization.live.ui.common.client.component.ComponentModel;
import com.organization.live.ui.common.client.data.BaseData;
import com.organization.live.ui.common.client.data.Data;
import com.organization.live.ui.common.client.util.Constants;
import com.organization.live.ui.common.client.widget.grid.GridModel;
import com.organization.live.ui.service.ServiceLocator;
import com.organization.live.ui.service.action.ActionHookSource;
import com.organization.live.ui.service.action.ExecutionSource;
import com.organization.live.ui.service.action.HookType;
import com.organization.live.ui.service.action.common.AbstractActionHook;
import com.organization.live.ui.system.action.executionsource.DataViewExecutionSource;
import com.organization.live.ui.system.component.dataview.DataViewAction;
import com.organization.live.ui.system.component.dataview.DataViewComponent;

/**
 * 
 * @author Danny Carvajal
 *
 */
public class PreSelectReclamationDataHook extends AbstractActionHook 
{

    @Override
    public ExecutionSource getExecutionSource() 
    {
        return new DataViewExecutionSource(Db.Computer.TABLE_NAME);
    }

    @Override
    public ActionHookSource getActionHookSource() 
    {
        return new ActionHookSource(DataViewAction.GET_DATA_VIEWS_PAGE_ACTION, HookType.POST_EXECUTE_HOOK);
    }

    @Override
    public ComponentModel hook(final ComponentModel model, final ServiceLocator serviceLocator) throws Exception 
    {
        GridModel gridModel = model.get(DataViewComponent.DATA_LIST);
        String component = model.getParameter(Constants.COMPONENT);
        List<Data> dataList = gridModel.getData();
                     
        if (DATA_PREVIEW.equals(component) && dataList != null) 
        {
            for (Data data : dataList) 
            {
                data.setChecked(true);
                data.setOriginal(BaseData.CHECKED, true);
            }
        }
        return model;
    }
}
