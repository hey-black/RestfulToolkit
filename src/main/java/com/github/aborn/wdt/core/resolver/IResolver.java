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
    List<RestServiceItem> getRestServiceItemList(Project project, Module module);
}
