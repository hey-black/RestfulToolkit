package com.zhaow.restful.common.resolver;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.zhaow.restful.annotations.JaxrsPathAnnotation;
import com.zhaow.restful.annotations.PathMappingAnnotation;
import com.zhaow.restful.annotations.SpringControllerAnnotation;
import com.zhaow.restful.annotations.SpringRequestMethodAnnotation;
import com.zhaow.restful.common.jaxrs.JaxrsAnnotationHelper;
import com.zhaow.restful.common.spring.RequestMappingAnnotationHelper;
import com.zhaow.restful.method.RequestPath;
import com.zhaow.restful.method.action.PropertiesHandler;
import com.zhaow.restful.navigation.action.RestServiceItem;
import org.jetbrains.kotlin.idea.stubindex.KotlinAnnotationsIndex;
import org.jetbrains.kotlin.psi.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SpringResolver extends BaseServiceResolver {

    PropertiesHandler propertiesHandler;

    public SpringResolver(Module module) {
        myModule = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    public SpringResolver(Project project) {
        myProject = project;

    }

    public SpringResolver(Project project, AnActionEvent anAction) {
        myProject = project;
        anActionEvent = anAction;

    }

    public List<RestServiceItem> getRestServiceItemList(Project project, Module module) {
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(SpringControllerAnnotation.REST_CONTROLLER.getShortName(), project, GlobalSearchScope.moduleScope(module));
        return buildRestServiceItemList(psiAnnotations);
    }

    public List<RestServiceItem> buildRestServiceItemList(Collection<PsiAnnotation> psiAnnotations) {
        List<RestServiceItem> itemList = new ArrayList<>();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) {
                continue;
            }

            PsiClass psiClass = (PsiClass) psiElement;
            PsiMethod[] psiMethods = psiClass.getMethods();

            if (psiMethods == null) {
                continue;
            }

            String classUriPath = JaxrsAnnotationHelper.getClassUriPath(psiClass);

            for (PsiMethod psiMethod : psiMethods) {
                RequestPath[] methodUriPaths = JaxrsAnnotationHelper.getRequestPaths(psiMethod);

                for (RequestPath methodUriPath : methodUriPaths) {
                    RestServiceItem item = createRestServiceItem(psiMethod, classUriPath, methodUriPath);
                    itemList.add(item);
                }
            }

        }

        return itemList;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();

        // TODO: 这种实现的局限了其他方式实现的url映射（xml（类似struts），webflux routers）
        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        for (PathMappingAnnotation controllerAnnotation : supportedAnnotations) {

            // java: 标注了 (Rest)Controller 注解的类，即 Controller 类
            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(controllerAnnotation.getShortName(), project, globalSearchScope);
            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
                PsiElement psiElement = psiModifierList.getParent();

                PsiClass psiClass = (PsiClass) psiElement;
                List<RestServiceItem> serviceItemList = getServiceItemList(psiClass);
                itemList.addAll(serviceItemList);
            }


            // kotlin:
            Collection<KtAnnotationEntry> ktAnnotationEntries = KotlinAnnotationsIndex.getInstance().get(controllerAnnotation.getShortName(), project, globalSearchScope);
            for (KtAnnotationEntry ktAnnotationEntry : ktAnnotationEntries) {
                KtClass ktClass = (KtClass) ktAnnotationEntry.getParent().getParent();

                List<RequestPath> classRequestPaths = getRequestPaths(ktClass);

                List<KtNamedFunction> ktNamedFunctions = getKtNamedFunctions(ktClass);
                for (KtNamedFunction fun : ktNamedFunctions) {
                    List<RequestPath> requestPaths = getRequestPaths(fun);

                    for (RequestPath classRequestPath : classRequestPaths) {
                        for (RequestPath requestPath : requestPaths) {
                            requestPath.concat(classRequestPath);
                            itemList.add(createRestServiceItem(fun, "", requestPath));
                        }
                    }
                }
            }
        }

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {

        PsiMethod[] psiMethods = psiClass.getMethods();
        if (psiMethods == null) {
            return new ArrayList<>();
        }

        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getRequestPaths(psiMethod);

            for (RequestPath classRequestPath : classRequestPaths) {
                for (RequestPath methodRequestPath : methodRequestPaths) {
                    String path = classRequestPath.getPath();

                    RestServiceItem item = createRestServiceItem(psiMethod, path, methodRequestPath);
                    itemList.add(item);
                }
            }

        }
        return itemList;
    }

    private List<KtNamedFunction> getKtNamedFunctions(KtClass ktClass) {
        List<KtNamedFunction> ktNamedFunctions = new ArrayList<>();
        List<KtDeclaration> declarations = ktClass.getDeclarations();

        for (KtDeclaration declaration : declarations) {
            if (declaration instanceof KtNamedFunction) {
                KtNamedFunction fun = (KtNamedFunction) declaration;
                ktNamedFunctions.add(fun);

            }
        }
        return ktNamedFunctions;
    }

    private List<RequestPath> getRequestPaths(KtClass ktClass) {
        String defaultPath = "/";
        //方法注解
        List<KtAnnotationEntry> annotationEntries = ktClass.getModifierList().getAnnotationEntries();

        List<RequestPath> requestPaths = getRequestMappings(defaultPath, annotationEntries);
        return requestPaths;
    }

    private List<RequestPath> getRequestPaths(KtNamedFunction fun) {
//        String methodBody = fun.getBodyExpression().getText();// 方法体
//        String defaultPath = fun.getName();
        String defaultPath = "/";
        //方法注解
        List<KtAnnotationEntry> annotationEntries = fun.getModifierList().getAnnotationEntries();
        List<RequestPath> requestPaths = getRequestMappings(defaultPath, annotationEntries);
        return requestPaths;
    }

    private List<RequestPath> getRequestMappings(String defaultPath, List<KtAnnotationEntry> annotationEntries) {
        List<RequestPath> requestPaths = new ArrayList<>();
        for (KtAnnotationEntry entry : annotationEntries) {
//            List<RequestPath> requestMappings = getRequestMappings(defaultPath, entry);
            List<RequestPath> requestMappings = getRequestMappings(defaultPath, entry);
            requestPaths.addAll(requestMappings);
        }
        return requestPaths;
    }

   /* private List<RequestPath> getRequestMappings(String defaultPath, KtAnnotationEntry entry) {
        List<RequestPath> requestPaths = new ArrayList<>();
        List<String> methodList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();

        String annotationName = entry.getCalleeExpression().getText();
        SpringRequestMethodAnnotation requestMethodAnnotation = SpringRequestMethodAnnotation.getByShortName(annotationName);
        if (requestMethodAnnotation == null) {
            return new ArrayList<>();
        }

        if (requestMethodAnnotation.methodName() != null) {
            methodList.add(requestMethodAnnotation.methodName());
        } else {
            // 下面循环获取
        }

        //注解参数值
        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();
        // 只有注解，没有参数
        if (valueArgumentList != null) {
            List<KtValueArgument> arguments = valueArgumentList.getArguments();


            for (int i = 0; i < arguments.size(); i++) {
                KtValueArgument ktValueArgument = arguments.get(i);
                KtValueArgumentName argumentName = ktValueArgument.getArgumentName();

                KtExpression argumentExpression = ktValueArgument.getArgumentExpression();
                if (argumentName == null || argumentName.getText().equals("value") || argumentName.getText().equals("path")) {
                    // array, kotlin 1.1-
                    if (argumentExpression.getText().startsWith("arrayOf")) {
                        List<KtValueArgument> pathValueArguments = ((KtCallExpression) argumentExpression).getValueArguments();
                        for (KtValueArgument pathValueArgument : pathValueArguments) {
                            pathList.add(pathValueArgument.getText().replace("\"", ""));
                        }
                        // array, kotlin 1.2+
                    } else if (argumentExpression.getText().startsWith("[")) {
                        List<KtExpression> innerExpressions = ((KtCollectionLiteralExpression) argumentExpression).getInnerExpressions();
                        for (KtExpression ktExpression : innerExpressions) {
                            pathList.add(ktExpression.getText().replace("\"", ""));
                        }
                    } else {
                        // 有且仅有一个value
                        PsiElement[] paths = ktValueArgument.getArgumentExpression().getChildren();
//                            Arrays.stream(paths).forEach(p -> pathList.add(p.getText()));
                        pathList.add(paths.length==0? "" : paths[0].getText());
                    }
                    //TODO
                    continue;
                }

                String attribute = "method";
                if (argumentName.getText().equals(attribute)) {

                    // array, kotlin 1.1-
                    if (argumentExpression.getText().startsWith("arrayOf")) {
                        List<KtValueArgument> pathValueArguments = ((KtCallExpression) argumentExpression).getValueArguments();
                        for (KtValueArgument pathValueArgument : pathValueArguments) {
                            methodList.add(pathValueArgument.getText().replace("\"", ""));
                        }
                        // array, kotlin 1.2+
                    } else if (argumentExpression.getText().startsWith("[")) {
                        List<KtExpression> innerExpressions = ((KtCollectionLiteralExpression) argumentExpression).getInnerExpressions();
                        for (KtExpression ktExpression : innerExpressions) {
                            methodList.add(ktExpression.getText().replace("\"", ""));
                        }
                    } else {
                        // 有且仅有一个value
                        PsiElement[] paths = ktValueArgument.getArgumentExpression().getChildren();
//                            Arrays.stream(paths).forEach(p -> methodList.add(p.getText()));
                        methodList.add(paths.length==0? "" : paths[0].getText());
                    }

                }
            }
        } else {
            pathList.add(defaultPath);
            //method = "GET";
        }

        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    requestPaths.add(new RequestPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                requestPaths.add(new RequestPath(path, null));
            }
        }

        return requestPaths;
    }*/


    private List<RequestPath> getRequestMappings(String defaultPath, KtAnnotationEntry entry) {
        List<RequestPath> requestPaths = new ArrayList<>();
        List<String> methodList = new ArrayList<>();
        List<String> pathList = new ArrayList<>();

        String annotationName = entry.getCalleeExpression().getText();
        SpringRequestMethodAnnotation requestMethodAnnotation = SpringRequestMethodAnnotation.getByShortName(annotationName);
        if (requestMethodAnnotation == null) {
            return new ArrayList<>();
        }

        if (requestMethodAnnotation.methodName() != null) { // GetMapping PostMapping ...
            methodList.add(requestMethodAnnotation.methodName());
        } else {
            methodList.addAll(getAttributeValues(entry, "method")); // RequestMapping
        }

        //注解参数值
//        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();
        if (entry.getValueArgumentList() != null) {
            List<String> mappingValues = getAttributeValues(entry, null);
            if (!mappingValues.isEmpty())
                pathList.addAll(mappingValues);
            else
                pathList.addAll(getAttributeValues(entry, "value")); // path

            pathList.addAll(getAttributeValues(entry, "path")); // path
        }

        if (pathList.isEmpty()) pathList.add(defaultPath); //没指定参数

        if (methodList.size() > 0) {
            for (String method : methodList) {
                for (String path : pathList) {
                    requestPaths.add(new RequestPath(path, method));
                }
            }
        } else {
            for (String path : pathList) {
                requestPaths.add(new RequestPath(path, null));
            }
        }

        return requestPaths;
    }

    private List<String> getAttributeValues(KtAnnotationEntry entry, String attribute) {
        KtValueArgumentList valueArgumentList = entry.getValueArgumentList();

        if (valueArgumentList == null) return Collections.emptyList();

        List<KtValueArgument> arguments = valueArgumentList.getArguments();

        for (int i = 0; i < arguments.size(); i++) {
            KtValueArgument ktValueArgument = arguments.get(i);
            KtValueArgumentName argumentName = ktValueArgument.getArgumentName();

            KtExpression argumentExpression = ktValueArgument.getArgumentExpression();

            if ((argumentName == null && attribute == null) || (argumentName != null && argumentName.getText().equals(attribute))) {
                List<String> methodList = new ArrayList<>();
                // array, kotlin 1.1-
                if (argumentExpression.getText().startsWith("arrayOf")) {
                    List<KtValueArgument> pathValueArguments = ((KtCallExpression) argumentExpression).getValueArguments();
                    for (KtValueArgument pathValueArgument : pathValueArguments) {
                        methodList.add(pathValueArgument.getText().replace("\"", ""));
                    }
                    // array, kotlin 1.2+
                } else if (argumentExpression.getText().startsWith("[")) {
                    List<KtExpression> innerExpressions = ((KtCollectionLiteralExpression) argumentExpression).getInnerExpressions();
                    for (KtExpression ktExpression : innerExpressions) {
                        methodList.add(ktExpression.getText().replace("\"", ""));
                    }
                } else {
                    // 有且仅有一个value
                    PsiElement[] paths = ktValueArgument.getArgumentExpression().getChildren();
//                            Arrays.stream(paths).forEach(p -> methodList.add(p.getText()));
                    methodList.add(paths.length == 0 ? "" : paths[0].getText());
                }

                return methodList;
            }
        }

        return new ArrayList<>();
    }


}
