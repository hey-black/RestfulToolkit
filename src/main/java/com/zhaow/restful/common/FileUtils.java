package com.zhaow.restful.common;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.source.PsiJavaFileImpl;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {

    /**
     * 提取java文件
     *
     * @param directory
     * @return
     */
    public static List<PsiClass> getJavaFilesFromDir(PsiDirectory directory) {
        List<PsiClass> psiClasses = new ArrayList<>();
        if ( directory != null) {
            for ( PsiFile file : directory.getFiles() ) {
                if ( !file.getFileType().getName().equals("JAVA") ) {
                    continue;
                }
                PsiClass[] classes = ((PsiJavaFileImpl) file).getClasses();

                if ( classes.length > 0) {
                    psiClasses.add( classes[0] );
                }
            }

            if ( directory.getSubdirectories() != null && directory.getSubdirectories().length > 0 ) {
                for ( PsiDirectory subDir : directory.getSubdirectories() ) {
                    psiClasses.addAll( getJavaFilesFromDir(subDir) );
                }
            }
        }
        return psiClasses;
    }
}
