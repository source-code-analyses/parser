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

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.FileFilterUtils

import java.io.File

enum class BuildSystem {
    GRADLE,
    MAVEN,
    UNKNOWN;

    companion object {
        @JvmStatic
        fun getBuildSystem(project: File): BuildSystem {
            if (!project.isDirectory) {
                return UNKNOWN
            }

            val gradleBuild: Int = FileUtils.listFiles(project,
            FileFilterUtils.nameFileFilter(BuildFiles.GRADLE_FILE.name),
            null).size
            val mavenBuild: Int = FileUtils.listFiles(project,
            FileFilterUtils.nameFileFilter(BuildFiles.MAVEN_FILE.name),
            null).size

            if (mavenBuild != 0) {
                return MAVEN
            }
            if (gradleBuild != 0) {
                return GRADLE
            }

            return UNKNOWN
        }
    }
}