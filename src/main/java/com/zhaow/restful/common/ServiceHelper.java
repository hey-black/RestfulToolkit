package com.zhaow.restful.common;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.annotations.JaxrsPathAnnotation;
import com.zhaow.restful.common.resolver.JaxrsResolver;
import com.zhaow.restful.common.resolver.ServiceResolver;
import com.zhaow.restful.common.resolver.SpringResolver;
import com.zhaow.restful.navigation.action.RestServiceItem;
import com.zhaow.restful.navigator.RestServiceProject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 服务相关工具类
 */
public class ServiceHelper {
    public static final Logger LOG = Logger.getInstance(ServiceHelper.class);
    PsiMethod psiMethod;

    public ServiceHelper(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public static List<RestServiceProject> buildRestServiceProjectListUsingResolver(Project project, AnActionEvent anActionEvent, Disposable disposable) {
        List<RestServiceProject> serviceProjectList = new ArrayList<>();
        Module[] modules = ModuleManager.getInstance(project).getModules();

        if (modules.length > 1) {
            for (Module module : modules) {
                List<RestServiceItem> restServices = buildRestServiceItemListUsingResolver(project, module, anActionEvent);
                if (restServices.size() > 0) {
                    serviceProjectList.add(new RestServiceProject(module.getName() + "(" + restServices.size() +")", restServices));
                }
            }
        } else {
            List<RestServiceItem> restServices = buildRestServiceItemListUsingResolver(project, anActionEvent);
            if (restServices.size() > 0) {
                serviceProjectList.add(new RestServiceProject(project.getName() + "(" + restServices.size() +")", restServices));
            }
        }

        return serviceProjectList;
    }

    @NotNull
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Project project, Module module, AnActionEvent anAction) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(project, anAction);
        JaxrsResolver jaxrsResolver = new JaxrsResolver(project, anAction);

        //ServiceResolver[] resolvers = {springResolver, jaxrsResolver};

        List<RestServiceItem> javaxPathList = jaxrsResolver.getRestServiceItemList(project, module);
        itemList.addAll(javaxPathList);

        List<RestServiceItem> springList = springResolver.getRestServiceItemList(project, module);
        itemList.addAll(springList);

        return itemList;
    }

    @NotNull
    public static List<RestServiceItem> buildRestServiceItemListUsingResolver(Project project, AnActionEvent anAction) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringResolver springResolver = new SpringResolver(project, anAction);
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
