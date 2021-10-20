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

package org.codeontology.docparser

import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.stream.Collectors
import kotlin.collections.ArrayList

public class DocCommentParser(comment: String) {
    private lateinit var comment: String
    private lateinit var tags: ArrayList<Tag>
    private var parsed: Boolean = false

    companion object {
        @JvmStatic public final val REGEXP: String = "^\\s*(@\\w+)\\s+(.*)$"
        @JvmStatic public final val PATTERN: Pattern = Pattern.compile(REGEXP, Pattern.DOTALL)
    }

    init {
        setComment(comment)
    }

    private fun setComment(comment: String) {
        parsed = false
        this.comment = comment
        tags = ArrayList()
    }

    public fun parse() {
        val scanner = Scanner("\n" + removeDescription())
        scanner.useDelimiter("\\n\\s*@")
        while(scanner.hasNext()) {
            val current = "@${scanner.next()}"
            val matcher: Matcher = getMatcher(current)

            if(matcher.matches()) {
                val name: String = matcher.group(1)
                val text: String = matcher.group(2)
                val tag: Tag = TagFactory.getInstance().createTag(name, text)
                this.tags.add(tag)
            }
        }

        scanner.close()
        parsed = true
    }

    private fun removeDescription(): String {
        val scanner = Scanner(this.comment)

        while(scanner.hasNext()) {
            var current: String = scanner.nextLine()

            if(getMatcher(current).matches()) {
                scanner.useDelimiter("\\Z")
                if(scanner.hasNext()) {
                    current += "\n" + scanner.next()
                }
                scanner.close()
                return current
            }
        }
        scanner.close()
        return ""
    }

    public fun getParamTags(): ArrayList<Tag> {
        return getTags(ParamTag.TAG)
    }

    public fun getReturnTags(): ArrayList<Tag> {
        return getTags(ReturnTag.TAG)
    }

    public fun getTags(name: String): ArrayList<Tag> {
        if(!parsed) {
            parse()
        }

        return tags.stream().filter { it.name.equals(name) }.collect(Collectors.toCollection(::ArrayList))
    }

    private fun getMatcher(current: String): Matcher {
        return PATTERN.matcher(current)
    }
}
