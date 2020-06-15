package com.github.aborn.wdt.core.helper;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zhaow.restful.annotations.SpringRequestMethodAnnotation;
import com.zhaow.restful.common.PsiAnnotationHelper;
import com.zhaow.restful.method.RequestPath;

import java.util.ArrayList;
import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 1:54 PM
 */
public class SpringAnnotationHelper implements AnnotationHelper {

    @Override
    public String getClassUriPath(PsiClass psiClass) {
        return null;
    }

    @Override
    public RequestPath[] getRequestPaths(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();

        if (annotations == null) {
            return null;
        }
        List<RequestPath> list = new ArrayList<>();

        for (PsiAnnotation annotation : annotations) {
            for (SpringRequestMethodAnnotation mappingAnnotation : SpringRequestMethodAnnotation.values()) {
//            for (PathMappingAnnotation mappingAnnotation : PathMappingAnnotation.allPathMappingAnnotations) {
                if (mappingAnnotation.getQualifiedName().endsWith(annotation.getQualifiedName())) {

//                    String defaultValue = psiMethod.getName();
                    String defaultValue = "/";
                    List<RequestPath> requestMappings = getRequestMappings(annotation, defaultValue);
                    if (requestMappings.size() > 0) {
                        list.addAll(requestMappings);
                    }
                }
            }
        }

        return list.toArray(new RequestPath[list.size()]);
    }

    private static List<RequestPath> getRequestMappings(PsiAnnotation annotation, String defaultValue) {
        List<RequestPath> mappingList = new ArrayList<>();

        List<String> methodList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "method");

        List<String> pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "value");
        if (pathList.size() == 0) {
            pathList = PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path");
        }

        // 没有设置 value，默认方法名
        if (pathList.size() == 0) {
            pathList.add(defaultValue);
        }

        // todo: 处理没有设置 value 或 path 的 RequestMapping

//        List<String> finalPathList = pathList;
//        methodList.forEach(method-> finalPathList.forEach(path->mappingList.add(new RequestMapping(path,method))));

        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    mappingList.add(new RequestPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                mappingList.add(new RequestPath(path, null));
            }
        }

        return mappingList;
    }
}
