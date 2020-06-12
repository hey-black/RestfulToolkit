package com.zhaow.restful.common.resolver;


import com.intellij.ide.IdeView;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.common.FileUtils;
import com.zhaow.restful.method.RequestPath;
import com.zhaow.restful.navigation.action.RestServiceItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseServiceResolver implements ServiceResolver {
    Module myModule;
    Project myProject;
    AnActionEvent anActionEvent;

    @Override
    public List<RestServiceItem> findAllSupportedServiceItemsInModule() {
        List<RestServiceItem> itemList = new ArrayList<>();
        if (myModule == null) {
            return itemList;
        }

        itemList = findAllSupportedServiceItemsInProject();
        return itemList;
    }

    public abstract List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope);

    @Override
    public List<RestServiceItem> findAllSupportedServiceItemsInProject() {
        List<RestServiceItem> itemList = null;
        if (myProject == null && myModule != null) {
            myProject = myModule.getProject();
        }

        if (anActionEvent == null) {
            return new ArrayList<>();
        }


        final IdeView view = anActionEvent.getData(LangDataKeys.IDE_VIEW);

        if (view == null) {
            return new ArrayList<>();
        }

        PsiDirectory directory = view.getOrChooseDirectory();

        List<PsiClass> allClasses = new ArrayList<>();
        if (directory != null) {
            allClasses.addAll(FileUtils.getJavaFilesFromDir(directory));
        }

        itemList = new ArrayList<>();
        for (PsiClass psiClass : allClasses) {
            itemList.addAll(Convertor.getServiceItemList(psiClass));
        }

        return itemList;

    }

    @NotNull
    protected RestServiceItem createRestServiceItem(PsiElement psiMethod, String classUriPath, RequestPath requestMapping) {
        if (!classUriPath.startsWith("/")) {
            classUriPath = "/".concat(classUriPath);
        }
        if (!classUriPath.endsWith("/")) {
            classUriPath = classUriPath.concat("/");
        }

        String methodPath = requestMapping.getPath();

        if (methodPath.startsWith("/")) {
            methodPath = methodPath.substring(1, methodPath.length());
        }
        String requestPath = classUriPath + methodPath;

        RestServiceItem item = new RestServiceItem(psiMethod, requestMapping.getMethod(), requestPath);
        if (myModule != null) {
            item.setModule(myModule);
        }
        return item;
    }

}
