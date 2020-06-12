package com.zhaow.restful.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;

import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * 提取java文件
     * @param project
     * @param directory
     * @return
     */
    public static List<PsiClass> getJavaFilesFromDir(Project project, PsiDirectory directory) {
        List<PsiClass> psiClasses = new ArrayList<>();
        if ( directory != null) {
            for ( PsiFile file : directory.getFiles() ) {
                if ( !file.getFileType().getName().equals("JAVA") ) {
                    continue;
                }

                String name = file.getName().split("\\.")[0];

                PsiClass[] classes = ((PsiJavaFileImpl) file).getClasses();
                //PsiClass[] classes = PsiShortNamesCache.getInstance(project).getClassesByName(name, GlobalSearchScope.fileScope(file));

                if ( classes.length > 0) {
                    psiClasses.add( classes[0] );
                }
            }

            if ( directory.getSubdirectories() != null && directory.getSubdirectories().length > 0 ) {
                for ( PsiDirectory subDir : directory.getSubdirectories() ) {
                    psiClasses.addAll( getJavaFilesFromDir(project, subDir) );
                }
            }
        }
        return psiClasses;
    }
}
