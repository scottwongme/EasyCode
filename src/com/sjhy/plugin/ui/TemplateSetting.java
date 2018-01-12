package com.sjhy.plugin.ui;

import com.intellij.openapi.options.Configurable;
import com.sjhy.plugin.entity.Template;
import com.sjhy.plugin.entity.TemplateGroup;
import com.sjhy.plugin.service.ConfigService;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplateSetting implements Configurable {
    private JPanel mainPanel;
    private JButton copyGroupButton;
    private JButton deleteButton;
    private JTabbedPane templateTabbedPane;
    private JComboBox groupButton;
    private JButton addButton;
    private JButton removeButton;

    private List<EditTemplatePanel> editTemplatePanelList = new ArrayList<>();
    private Map<String, TemplateGroup> templateGroupMap;
    private String currGroupName;

    private ConfigService configService;

    private Boolean init = false;

    private TemplateGroup getTemplateGroup() {
        return this.templateGroupMap.get(currGroupName);
    }

    TemplateSetting(ConfigService configService) {
        this.configService = configService;

        //新增选项卡
        addButton.addActionListener(e -> {
            String value = JOptionPane.showInputDialog(null, "Input Tab Name:", "Demo");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Tab Name Can't Is Empty!");
                return;
            }
            for (Template template : getTemplateGroup().getElementList()) {
                if (template.getName().equals(value)){
                    JOptionPane.showMessageDialog(null, "Tab Name Already exist!");
                    return;
                }
            }
            getTemplateGroup().getElementList().add(new Template(value, ""));
            refresh();
        });
        //删除选项卡
        removeButton.addActionListener(e -> {
            getTemplateGroup().getElementList().remove(templateTabbedPane.getSelectedIndex());
            refresh();
        });

        //切换分组
        this.groupButton.addActionListener(e -> {
            if (!init) {
                return;
            }
            String val = (String) groupButton.getSelectedItem();
            if (val==null) {
                return;
            }
            if (val.equals(currGroupName)){
                return;
            }
            currGroupName = val;
            refresh();
        });
        //复制分组
        copyGroupButton.addActionListener(e -> {
            String value = JOptionPane.showInputDialog(null, "Input Group Name:", currGroupName+" Copy");
            if (value==null) {
                return;
            }
            if (value.trim().length()==0){
                JOptionPane.showMessageDialog(null, "Group Name Can't Is Empty!");
                return;
            }
            if (templateGroupMap.containsKey(value)){
                JOptionPane.showMessageDialog(null, "Group Name Already exist!");
                return;
            }
            TemplateGroup templateGroup = templateGroupMap.get(currGroupName).clone();
            templateGroup.setName(value);
            templateGroupMap.put(value, templateGroup);
            currGroupName = value;
            refresh();
        });
        //删除分组
        deleteButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(null, "Confirm Delete Group "+currGroupName+"?", "温馨提示", JOptionPane.OK_CANCEL_OPTION);
            if (result==0){
                if(ConfigService.DEFAULT_NAME.equals(currGroupName)){
                    JOptionPane.showMessageDialog(null, "Can't Delete Default Group!");
                    return;
                }
                templateGroupMap.remove(currGroupName);
                currGroupName = ConfigService.DEFAULT_NAME;
                refresh();
            }
        });
        init();
    }

    private void init() {
        //复制一份
        this.templateGroupMap = new LinkedHashMap<>();
        configService.getTemplateGroupMap().forEach((s, templateGroup) -> this.templateGroupMap.put(s, templateGroup.clone()));
        this.currGroupName = configService.getCurrTemplateGroupName();
        refresh();
    }

    @SuppressWarnings("unchecked")
    private void refresh() {
        this.init = false;
        //初始化分组
        this.groupButton.removeAllItems();
        this.templateGroupMap.keySet().forEach(this.groupButton::addItem);
        this.groupButton.setSelectedItem(this.currGroupName);
        //初始化选项卡
        editTemplatePanelList.clear();
        int count = this.templateTabbedPane.getTabCount();
        if (count>0) {
            for (int i = 0; i < count; i++) {
                this.templateTabbedPane.removeTabAt(0);
            }
        }
        getTemplateGroup().getElementList().forEach(template -> {
            EditTemplatePanel editTemplatePanel = new EditTemplatePanel(template);
            editTemplatePanelList.add(editTemplatePanel);
            this.templateTabbedPane.addTab(template.getName(), editTemplatePanel.getMainPanel());
        });
        this.init = true;
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Template Setting";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        return this.mainPanel;
    }

    @Override
    public boolean isModified() {
        editTemplatePanelList.forEach(EditTemplatePanel::refresh);
        return !configService.getTemplateGroupMap().equals(this.templateGroupMap);
    }

    @Override
    public void apply() {
        this.configService.setCurrTemplateGroupName(currGroupName);
        this.configService.setTemplateGroupMap(templateGroupMap);
    }

    @Override
    public void reset() {
        init();
    }
}