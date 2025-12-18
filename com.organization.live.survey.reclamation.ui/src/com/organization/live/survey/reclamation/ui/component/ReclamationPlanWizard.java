package com.organization.live.survey.reclamation.ui.component;

import com.organization.live.ui.common.client.component.descriptor.*;
import com.organization.live.ui.common.client.widget.component.ComponentWidgetDescriptor;
import com.organization.live.ui.common.client.widget.form.FormDescriptor;
import com.organization.live.ui.common.client.widget.panel.PanelDescriptor;
import com.organization.live.ui.service.component.AbstractComponent;

/**
 * @author Danny Carvajal- example used was com.sclable.live.impex.component.ImportMappingWizard.java
 *
 */
public class ReclamationPlanWizard extends AbstractComponent implements ReclamationPlanComponent 
{
    
    @Override
    public String getName() 
    {
        return COMPONENT_NAME;
    }
    
    @Override
    protected ComponentView getView() 
    {
        final Wizard wizard = new Wizard();
        
        // Scene 1
        final SceneDescriptor cfgScene1 = new SceneDescriptor(SCENE_TITLE_SELECT_COMPUTERS, SCENE1_ICON, SCENE1_INITIALIZER);
        cfgScene1.setSelectable(false);
        cfgScene1.setReloadOnReturn(false);
        final FormDescriptor cfgScene1Form = new FormDescriptor(SCENE_SELECT_COMPUTERS_FORM);
        cfgScene1Form.setHasCancel(false);
        cfgScene1Form.getColumnStyle().setLabelsWidth(20);
        cfgScene1Form.setPixelWidth(950);
        cfgScene1.getPanel().addWidget(cfgScene1Form);        

        final ComponentWidgetDescriptor dataPreview = new ComponentWidgetDescriptor(DATA_PREVIEW);
        cfgScene1.getPanel().addWidget(dataPreview);
  
        // Scene 2
        final SceneDescriptor cfgScene2 = new SceneDescriptor(SCENE_TITLE_CONNECTION, SCENE2_ICON, SCENE2_INITIALIZER);
        cfgScene2.setSelectable(false);
        cfgScene2.setReloadOnReturn(false);
        final FormDescriptor cfgScene2Form = new FormDescriptor(SCENE_CONNECTION_FORM);
        cfgScene2Form.setHasCancel(false);
        cfgScene2Form.getColumnStyle().setLabelsWidth(20);
        cfgScene2Form.setStyleName("centered");
        cfgScene2Form.setPixelWidth(700);
        cfgScene2.getPanel().addWidget(cfgScene2Form);     
        
        // Scene 3
        final SceneDescriptor cfgScene3 = new SceneDescriptor(SCENE_TITLE_DEPLOYMENTS, SCENE3_ICON, SCENE3_INITIALIZER);
        cfgScene3.setSelectable(false);
        cfgScene3.setReloadOnReturn(false);
        final FormDescriptor cfgScene3Form = new FormDescriptor(SCENE_DEPLOYMENTS_FORM);
        cfgScene3Form.setHasCancel(false);
        cfgScene3Form.getColumnStyle().setLabelsWidth(25);
        cfgScene3Form.setStyleName("centered");
        cfgScene3Form.setPixelWidth(800);
        cfgScene3.getPanel().addWidget(cfgScene3Form);          
        
        // Scene 4
        final SceneDescriptor cfgScene4 = new SceneDescriptor(SCENE_TITLE_CONFIGURATION, SCENE4_ICON, SCENE4_INITIALIZER);
        cfgScene4.setSelectable(false);
        cfgScene4.setReloadOnReturn(false);
        final FormDescriptor cfgScene4Form = new FormDescriptor(SCENE_CONFIGURATION_FORM);
        cfgScene4Form.setHasCancel(false);
        cfgScene4Form.getColumnStyle().setLabelsWidth(25);
        cfgScene4Form.setStyleName("centered");
        cfgScene4Form.setPixelWidth(700);
        cfgScene4.getPanel().addWidget(cfgScene4Form);             

        // Scene 5
        final SceneDescriptor cfgScene5 = new SceneDescriptor(SCENE_TITLE_EMAIL, SCENE5_ICON, SCENE5_INITIALIZER);
        cfgScene5.setSelectable(false);
        cfgScene5.setReloadOnReturn(false);
        
        final FormDescriptor cfgMsgForm = new FormDescriptor(SCENE_EMAIL_FORM_MSG);
        cfgMsgForm.getColumnStyle().setLabelsWidth(25);
        cfgMsgForm.setStyleName("centered");
        cfgMsgForm.setPixelWidth(700);
        cfgScene5.getPanel().addWidget(cfgMsgForm);
        
        final FormDescriptor cfgScene5Form = new FormDescriptor(SCENE_EMAIL_FORM);
        cfgScene5Form.setHasCancel(false);
        cfgScene5Form.getColumnStyle().setLabelsWidth(25);
        cfgScene5Form.setStyleName("centered");
        cfgScene5Form.setPixelWidth(700);
        cfgScene5.getPanel().addWidget(cfgScene5Form);         
        
        // Scene 6
        final SceneDescriptor cfgScene6 = new SceneDescriptor(SCENE_TITLE_REVIEW_SAVE, SCENE6_ICON, SCENE6_INITIALIZER);
        cfgScene6.setSelectable(false);
        cfgScene6.setReloadOnReturn(false);
        final FormDescriptor cfgScene6Form = new FormDescriptor(SCENE_REVIEW_SAVE_FORM);
        cfgScene6Form.setHasCancel(false);
        cfgScene6Form.getColumnStyle().setLabelsWidth(30);
        cfgScene6Form.setStyleName("centered");
        cfgScene6Form.setPixelWidth(700);
        cfgScene6.getPanel().addWidget(cfgScene6Form);   
        
        final PopUpStateDescriptor popupState = new PopUpStateDescriptor("PopUp");
        popupState.setPercentHeight(0);
        popupState.setPercentWidth(60);
        popupState.setPixelWidth(200);
        popupState.setPixelHeight(200);
        popupState.setCloseAction(null);
        final PanelDescriptor panel = popupState.getPanel();
        panel.setHeader(null);
        panel.getWidgets().clear();
        
        wizard.addScene(cfgScene1);
        wizard.addState(popupState);
        wizard.addScene(cfgScene2);
        wizard.addScene(cfgScene3);
        wizard.addScene(cfgScene4);
        wizard.addScene(cfgScene5);
        wizard.addScene(cfgScene6);
        
        wizard.setFinishAction(FINISH_ACTION);        
        wizard.setFinishButtonLabel("Save and Finish");  
        return wizard;
    }
}
