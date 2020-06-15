package com.github.aborn.wdt.core.resolver;

import com.github.aborn.wdt.core.helper.AnnotationHelper;
import com.intellij.psi.PsiElement;
import com.zhaow.restful.method.RequestPath;
import com.zhaow.restful.navigation.action.RestServiceItem;
import org.jetbrains.annotations.NotNull;

/**
 * @author aborn
 * @date 2020/06/15 11:41 AM
 */
public abstract class BaseResolver implements IResolver {
    protected AnnotationHelper annotationHelper;

    @NotNull
    protected RestServiceItem buildRestServiceItem(PsiElement psiMethod, String classUriPath, RequestPath requestMapping) {
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
        return new RestServiceItem(psiMethod, requestMapping.getMethod(), requestPath);
    }
}
