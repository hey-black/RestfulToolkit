package com.github.aborn.wdt.core.resolver;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.zhaow.restful.navigation.action.RestServiceItem;

import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 11:26 AM
 */
public interface IResolver {
    /**
     * build data from module in project
     * @param project
     * @param module
     * @return
     */
    List<RestServiceItem> getRestServiceItemList(Project project, Module module);

    /**
     * build data from all project
     * @param project
     * @return
     */
    List<RestServiceItem> getRestServiceItemList(Project project);
}
