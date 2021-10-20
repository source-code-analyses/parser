package org.codeontology.build.gradle

import org.codeontology.extraction.project.ProjectVisitor
import org.codeontology.build.DependenciesLoader

import java.io.File

public class AndroidProject(project: File): GradleProject(project) {
    private var loader: AndroidLoader? = null

    public override fun getLoader(): DependenciesLoader<GradleProject> {
        if (loader == null) {
            loader = AndroidLoader(this)
        }
        return loader!!
    }

    public override fun accept(visitor: ProjectVisitor) {
        visitor.visit(this)
    }
}