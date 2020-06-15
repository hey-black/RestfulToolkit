package com.github.aborn.wdt.core;

import com.github.aborn.wdt.core.resolver.IResolver;
import com.github.aborn.wdt.core.resolver.JavaxrsResolver;
import com.github.aborn.wdt.core.resolver.SpringResolver;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.common.ServiceHelper;
import com.zhaow.restful.common.resolver.JaxrsResolver;
import com.zhaow.restful.navigation.action.RestServiceItem;
import com.zhaow.restful.navigator.RestServiceProject;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 11:35 AM
 */
public class RestServiceDataManager {

    public static final Logger LOG = Logger.getInstance(ServiceHelper.class);

    public static List<RestServiceProject> buildRestServiceData(Project project, AnActionEvent anActionEvent) {
        List<RestServiceProject> serviceProjectList = Lists.newArrayList();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        IResolver[] resolvers = {new SpringResolver(), new JavaxrsResolver()};


        if (modules.length > 0) {
            for (Module module : modules) {
                List<RestServiceItem> restServices = Lists.newArrayList();
                for (IResolver resolver : resolvers) {
                    restServices.addAll(resolver.getRestServiceItemList(project, module));
                }

                if (restServices.size() > 0) {
                    serviceProjectList.add(new RestServiceProject(module.getName() + "(" + restServices.size() +")", restServices));
                }
            }
        } else {
            List<RestServiceItem> restServices = buildRestServiceItemListUsingResolver(project, anActionEvent);
            if (restServices.size() > 0) {
                serviceProjectList.add(new RestServiceProject(project.getName() + "(" + restServices.size() +")", restServices));
            }
            LOG.info("Not find any modules in project " + project.getName() + ".");
        }

        return serviceProjectList;
    }

    // TODO 需要重构
    @NotNull
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Project project, AnActionEvent anAction) {
        List<RestServiceItem> itemList = Lists.newArrayList();

        com.zhaow.restful.common.resolver.SpringResolver springResolver = new com.zhaow.restful.common.resolver.SpringResolver(project, anAction);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(project, anAction);

        //ServiceResolver[] resolvers = {springResolver, jaxrsResolver};

        List<RestServiceItem> javaxPathList = jaxrsResolver.getRestServiceItemList(project, GlobalSearchScope.allScope(project));
        itemList.addAll(javaxPathList);
        List<RestServiceItem> springList = springResolver.findAllSupportedServiceItemsInProject();
        itemList.addAll(springList);

//        for (ServiceResolver resolver : resolvers) {
//            List<RestServiceItem> allSupportedServiceItemsInProject = resolver.findAllSupportedServiceItemsInProject();
//
//            itemList.addAll(allSupportedServiceItemsInProject);
//        }

        return itemList;
    }
}
