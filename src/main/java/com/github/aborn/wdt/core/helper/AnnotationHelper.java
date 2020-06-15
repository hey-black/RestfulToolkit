package com.github.aborn.wdt.core.helper;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.zhaow.restful.method.RequestPath;

/**
 * @author aborn
 * @date 2020/06/15 12:02 PM
 */
public interface AnnotationHelper {

    /**
     * get class uri path
     * @param psiClass
     * @return
     */
    String getClassUriPath(PsiClass psiClass);

    /**
     *
     * @param psiMethod
     * @return
     */
    RequestPath[] getRequestPaths(PsiMethod psiMethod);
}
