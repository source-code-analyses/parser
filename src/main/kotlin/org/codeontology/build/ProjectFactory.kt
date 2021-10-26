/*
Copyright 2021 Mattia Atzeni, Maurizio Atzori, Oliver Schmidtke
This file is part of CodeOntology.
CodeOntology is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
CodeOntology is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.
You should have received a copy of the GNU General Public License
along with CodeOntology.  If not, see <http://www.gnu.org/licenses/>
*/

package org.codeontology.build

import org.codeontology.build.gradle.AndroidProject
import org.codeontology.build.gradle.GradleProject
import org.codeontology.build.maven.MavenProject

import java.io.File

class ProjectFactory {
    companion object {
        @JvmStatic private var instance: ProjectFactory? = null
        @JvmStatic
        fun getInstance(): ProjectFactory {
            if (instance == null) {
                instance = ProjectFactory()
            }

            return instance as ProjectFactory
        }
    }

    fun getProject(path: String): Project {
        return getProject(File(path))
    }

    fun getProject(project: File): Project {
        return when(BuildSystem.getBuildSystem(project)) {
            BuildSystem.MAVEN -> MavenProject(project)
            BuildSystem.GRADLE -> if (File(project.path + "/src/main/AndroidManifest.xml").exists()) {
                AndroidProject(project)
            } else {
                GradleProject(project)
            }
            else -> DefaultProject(project)
        }
    }
}