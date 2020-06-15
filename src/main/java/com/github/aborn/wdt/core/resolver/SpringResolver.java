package com.github.aborn.wdt.core.resolver;

import com.github.aborn.wdt.core.helper.SpringAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.annotations.SpringControllerAnnotation;
import com.zhaow.restful.common.spring.RequestMappingAnnotationHelper;
import com.zhaow.restful.method.RequestPath;
import com.zhaow.restful.navigation.action.RestServiceItem;
import org.apache.commons.compress.utils.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 11:50 AM
 */
public class SpringResolver extends BaseResolver {

    public SpringResolver() {
        annotationHelper = new SpringAnnotationHelper();
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project) {
        Collection<PsiAnnotation> psiAnnotations = new ArrayList<>();
        for (SpringControllerAnnotation annotation : SpringControllerAnnotation.values()) {
            psiAnnotations.addAll(JavaAnnotationIndex.getInstance().get(annotation.getShortName(), project, GlobalSearchScope.projectScope(project)));
        }
        return build(psiAnnotations, null);
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, Module module) {
        Collection<PsiAnnotation> psiAnnotations = new ArrayList<>();
        for (SpringControllerAnnotation annotation : SpringControllerAnnotation.values()) {
            psiAnnotations.addAll(JavaAnnotationIndex.getInstance().get(annotation.getShortName(), project, GlobalSearchScope.moduleScope(module)));
        }
        return build(psiAnnotations, module);
    }

    private List<RestServiceItem> build(Collection<PsiAnnotation> psiAnnotations, Module module) {

        List<RestServiceItem> itemList = Lists.newArrayList();

        psiAnnotations.forEach(psiAnnotation -> {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) {
                return;
            }

            PsiClass psiClass = (PsiClass) psiElement;
            PsiMethod[] psiMethods = psiClass.getMethods();

            if (psiMethods == null) {
                return;
            }

            List<RestServiceItem> restServiceItems = getServiceItemList(psiClass, module);

            itemList.addAll(restServiceItems);

        });

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass, Module module) {

        PsiMethod[] psiMethods = psiClass.getMethods();
        if (psiMethods == null) {
            return new ArrayList<>();
        }

        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = annotationHelper.getRequestPaths(psiMethod);

            for (RequestPath classRequestPath : classRequestPaths) {
                for (RequestPath methodRequestPath : methodRequestPaths) {
                    String path = classRequestPath.getPath();

                    RestServiceItem item = buildRestServiceItem(psiMethod, path, methodRequestPath);
                    if (module != null) {
                        item.setModule(module);
                    }
                    itemList.add(item);
                }
            }

        }
        return itemList;
    }


}
