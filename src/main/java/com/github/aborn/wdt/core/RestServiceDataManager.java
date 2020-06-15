package com.github.aborn.wdt.core;

import com.github.aborn.wdt.core.resolver.IResolver;
import com.github.aborn.wdt.core.resolver.JavaxrsResolver;
import com.github.aborn.wdt.core.resolver.SpringResolver;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.zhaow.restful.common.ServiceHelper;
import com.zhaow.restful.navigation.action.RestServiceItem;
import com.zhaow.restful.navigator.RestServiceProject;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 11:35 AM
 */
public class RestServiceDataManager {

    public static final Logger LOG = Logger.getInstance(ServiceHelper.class);

    public static List<RestServiceProject> buildRestServiceData(Project project) {
        List<RestServiceProject> serviceProjectList = Lists.newArrayList();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        if (modules.length > 0) {
            for (Module module : modules) {
                List<RestServiceItem> restServices = buildRestServiceItemListFrom(project, module);
                if (restServices.size() > 0) {
                    serviceProjectList.add(new RestServiceProject(module.getName() + "(" + restServices.size() +")", restServices));
                }
            }
        } else {
            List<RestServiceItem> restServices = buildRestServiceItemListFrom(project);
            if (restServices.size() > 0) {
                serviceProjectList.add(new RestServiceProject(project.getName() + "(" + restServices.size() +")", restServices));
            }
            LOG.info("Not find any modules in project " + project.getName() + ".");
        }

        return serviceProjectList;
    }

    /**
     * from module
     * @param project
     * @param module
     * @return
     */
    public static List<RestServiceItem> buildRestServiceItemListFrom(Project project, Module module) {
        IResolver[] resolvers = {new SpringResolver(), new JavaxrsResolver()};
        List<RestServiceItem> restServices = Lists.newArrayList();
        for (IResolver resolver : resolvers) {
            restServices.addAll(resolver.getRestServiceItemList(project, module));
        }
        return restServices;
    }

    /**
     * from project
     * @param project
     * @return
     */
    public static List<RestServiceItem> buildRestServiceItemListFrom(Project project) {
        IResolver[] resolvers = {new SpringResolver(), new JavaxrsResolver()};
        List<RestServiceItem> restServices = Lists.newArrayList();
        for (IResolver resolver : resolvers) {
            restServices.addAll(resolver.getRestServiceItemList(project));
        }
        return restServices;
    }
}
