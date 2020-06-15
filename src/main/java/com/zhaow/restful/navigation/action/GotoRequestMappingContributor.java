package com.zhaow.restful.navigation.action;


import com.github.aborn.wdt.core.RestServiceDataManager;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GotoRequestMappingContributor implements ChooseByNameContributor {
    Module myModule;

    private List<RestServiceItem> navItem;

    public GotoRequestMappingContributor(Module myModule) {
        this.myModule = myModule;
    }

    @NotNull
    @Override
    public String[] getNames(Project project, boolean onlyThisModuleChecked) {
        String[] names = null;
        List<RestServiceItem> itemList;

        // data source
        if (onlyThisModuleChecked && myModule != null) {
            itemList = RestServiceDataManager.buildRestServiceItemListFrom(project, myModule);
        } else {
            itemList = RestServiceDataManager.buildRestServiceItemListFrom(project);
        }

        navItem = itemList;

        if (itemList != null) {
            names = new String[itemList.size()];
        }

        for (int i = 0; i < itemList.size(); i++) {
            RestServiceItem requestMappingNavigationItem = itemList.get(i);
            names[i] = requestMappingNavigationItem.getName();
        }

        return names;
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean onlyThisModuleChecked) {
        NavigationItem[] navigationItems = navItem.stream().filter(item -> item.getName().equals(name)).toArray(NavigationItem[]::new);
        return navigationItems;

    }
}
