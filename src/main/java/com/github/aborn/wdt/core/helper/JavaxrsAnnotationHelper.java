package com.github.aborn.wdt.core.helper;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zhaow.restful.annotations.JaxrsHttpMethodAnnotation;
import com.zhaow.restful.annotations.JaxrsPathAnnotation;
import com.zhaow.restful.common.PsiAnnotationHelper;
import com.zhaow.restful.method.RequestPath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author aborn
 * @date 2020/06/15 12:03 PM
 */
public class JavaxrsAnnotationHelper implements AnnotationHelper {

    @Override
    public String getClassUriPath(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getModifierList().findAnnotation(JaxrsPathAnnotation.PATH.getQualifiedName());
        String path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");
        return path != null ? path : "";
    }

    @Override
    public RequestPath[] getRequestPaths(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
        if(annotations == null) return null;
        List<RequestPath> list = new ArrayList<>();

        PsiAnnotation wsPathAnnotation = psiMethod.getModifierList().findAnnotation(JaxrsPathAnnotation.PATH.getQualifiedName());
        String path = wsPathAnnotation == null ? psiMethod.getName() : getWsPathValue(wsPathAnnotation);

        JaxrsHttpMethodAnnotation[] jaxrsHttpMethodAnnotations = JaxrsHttpMethodAnnotation.values();

        /*for (PsiAnnotation annotation : annotations) {
            for (JaxrsHttpMethodAnnotation methodAnnotation : jaxrsHttpMethodAnnotations) {
                if (annotation.getQualifiedName().equals(methodAnnotation.getQualifiedName())) {
                    list.add(new RequestPath(path, methodAnnotation.getShortName()));
                }
            }
        }*/

        Arrays.stream(annotations).forEach(a-> Arrays.stream(jaxrsHttpMethodAnnotations).forEach(methodAnnotation-> {
            if (a.getQualifiedName().equals(methodAnnotation.getQualifiedName())) {
                list.add(new RequestPath(path, methodAnnotation.getShortName()));
            }
        }));

        return list.toArray(new RequestPath[list.size()]);
    }

    private String getWsPathValue(PsiAnnotation annotation) {
        String value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");

        return value != null ? value : "";
    }
}
