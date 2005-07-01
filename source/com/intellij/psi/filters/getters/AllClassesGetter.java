package com.intellij.psi.filters.getters;

import com.intellij.codeInsight.completion.CompletionContext;
import com.intellij.codeInsight.completion.CompletionUtil;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.filters.ContextGetter;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiShortNamesCache;
import com.intellij.util.ArrayUtil;

import java.util.*;

import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: ik
 * Date: 02.12.2003
 * Time: 16:49:25
 * To change this template use Options | File Templates.
 */
public class AllClassesGetter implements ContextGetter{
  private String myPrefixStr = null;
  private PatternMatcher myMatcher = null;
  private Pattern myPattern = null;

  public Object[] get(PsiElement context, CompletionContext completionContext) {
    if(context == null || !context.isValid()) return ArrayUtil.EMPTY_OBJECT_ARRAY;

    final List<PsiClass> classesList = new ArrayList<PsiClass>();
    final PsiManager manager = context.getManager();
    final PsiShortNamesCache cache = manager.getShortNamesCache();
    // Optimization:
    final String prefix = context.getUserData(CompletionUtil.COMPLETION_PREFIX);

    if(myPrefixStr != prefix){
      myMatcher = new Perl5Matcher();
      myPattern = CompletionUtil.createCampelHumpsMatcher(prefix);
    }

    final GlobalSearchScope scope = context.getContainingFile().getResolveScope();

    final String[] names = cache.getAllClassNames(true);
    for (final String name : names) {
      if (prefix != null && !(CompletionUtil.checkName(name, prefix) || myMatcher.matches(name, myPattern))) continue;
      classesList.addAll(Arrays.asList(cache.getClassesByName(name, scope)));
    }

    Collections.sort(classesList, new Comparator<PsiClass>() {
      public int compare(PsiClass psiClass, PsiClass psiClass1) {
        if(manager.areElementsEquivalent(psiClass, psiClass1)) return 0;

        return getClassIndex(psiClass) - getClassIndex(psiClass1);
      }

      private int getClassIndex(PsiClass psiClass){
        if(psiClass.getManager().isInProject(psiClass)) return 2;
        final String qualifiedName = psiClass.getQualifiedName();
        if(qualifiedName.startsWith("java.") || qualifiedName.startsWith("javax.")) return 1;
        return 0;
      }

      public boolean equals(Object o) {
        return o == this;
      }
    });

    return classesList.toArray(PsiClass.EMPTY_ARRAY);
  }
}
