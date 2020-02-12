package org.mozilla.firefox.vpn

import com.github.h0tk3y.betterParse.combinators.*
import com.github.h0tk3y.betterParse.grammar.Grammar
import com.github.h0tk3y.betterParse.grammar.parseToEnd
import com.github.h0tk3y.betterParse.grammar.parser
import com.github.h0tk3y.betterParse.parser.parseToEnd
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.lang.StringBuilder
import java.util.regex.Pattern
import kotlin.math.min

open class CompileFluentTask : DefaultTask() {

    private val rootProject
        get() = project.rootProject

    private val ext
        get() = rootProject.extensions.extraProperties.properties

    private val placeholderOrderMap = mutableMapOf<String, Map<String, Int>>()

    init {
        description = "Compile ftl file into Android string resources"
    }

    @TaskAction
    fun compileFluent() {
        val localeToStrings = buildStringMap()
        for (key in localeToStrings.keys) {
            val localeDir = when (key) {
                "en-us" -> { "values" }
                "de" -> "values-de"
                else -> null
            } ?: continue

            val values = localeToStrings[key] ?: continue
            if (placeholderOrderMap.isEmpty() && key == "en-us") {
                placeholderOrderMap.putAll(values.buildPlaceholderOrderMap())
            }

            val resolved = values
                .resolveInlinedVariable()
                .resolvePlaceholder()
                .resolveEscapedStrings()
                .customizeSpecialStrings()

            val resContent = buildStringResource(resolved)

            val resPath = "./app/src/main/res/${localeDir}/strings_ftl.xml"
            File(resPath).apply {
                parentFile.mkdirs()
                writeText(resContent)
            }
        }
    }

    private fun List<Attribute>.resolveInlinedVariable(): List<Attribute> {
        return mapStringValues { _, str ->
            resolvePlaceholder(str) { resolveStringVariable(it) }
        }
    }

    private fun List<Attribute>.resolvePlaceholder(): List<Attribute> {
        return mapSimpleStringValues { id, str ->
            if (placeholderOrderMap.containsKey(id)) {
                var newStr = str
                placeholderOrderMap[id]?.forEach { placeholder, idx ->
                    val ph = if (placeholderOrderMap[id]?.count() == 1) {
                        "%s"
                    } else {
                        "%${idx + 1}\$s"
                    }
                    newStr = newStr.replace(placeholder, ph)
                }
                newStr
            } else {
                str
            }
        }
    }

    private fun List<Attribute>.resolveEscapedStrings(): List<Attribute> {
        return mapStringValues { _, str ->
            resolveEscapedStrings(str)
        }
    }

    private fun List<Attribute>.customizeSpecialStrings(): List<Attribute> {
        return this
    }

    private fun List<Attribute>.mapStringValues(function: (String, String) -> String): List<Attribute> {
        return mapSimpleStringValues(function).mapPluralStringValues(function)
    }

    private fun List<Attribute>.mapSimpleStringValues(function: (id: String, str: String) -> String): List<Attribute> {
        return map {
            when (it.value) {
                is StringValue.SimpleString -> Attribute(it.id, StringValue.SimpleString(function(it.id, it.value.value)))
                else -> it
            }
        }
    }

    private fun List<Attribute>.mapPluralStringValues(function: (String, String) -> String): List<Attribute> {
        return map { attr ->
            when (attr.value) {
                is StringValue.Plural -> {
                    Attribute(attr.id, StringValue.Plural(attr.value.variable, attr.value.map.mapValues {
                        function(attr.id, it.value)
                    }))
                }
                else -> attr
            }
        }
    }

    private fun List<Attribute>.buildPlaceholderOrderMap(): Map<String, Map<String, Int>> {
        return mapNotNull { attr ->
            if (attr.value is StringValue.SimpleString) {
                val str = attr.value.value
                val regex = Regex("""\{\$[a-zA-Z][a-zA-Z\-_0-9]*}""")
                val matches = regex.findAll(str)
                matches.takeIf { it.count() > 0 }
                    ?.mapIndexed { index, matchResult -> matchResult.value to index }
                    ?.toMap()
                    ?.let {
                        attr.id to it
                    }
            } else {
                null
            }
        }.toMap()
    }

    private fun List<Attribute>.resolveStringVariable(id: String): String? {
        val attr = find { it.id == id }?.value
        return if (attr is StringValue.SimpleString) {
            attr.value
        } else {
            null
        }
    }

    private fun resolvePlaceholder(str: String, resolver: (name: String) -> String?): String {
        val varPattern = """\{([a-zA-Z][a-zA-Z0-9\-_]*)}"""
        val matcher = Pattern.compile(varPattern).matcher(str)
        var replaced = str
        while (matcher.find()) {
            val varName = matcher.group(1)
            val varValue = resolver(varName) ?: continue
            replaced = replaced.replace("{${varName}}", varValue)
        }
        return replaced
    }

    private fun resolveEscapedStrings(str: String): String {
        val varPattern = """\{"(.+)"}"""
        val matcher = Pattern.compile(varPattern).matcher(str)
        var replaced = str
        while (matcher.find()) {
            val escapedContent = matcher.group(1)
            replaced = replaced.replace("{\"${escapedContent}\"}", escapedContent)
        }
        return replaced
    }

    private fun buildStringResource(attrs: List<Attribute>): String {
        if (attrs.isEmpty()) { return "" }
        val sb = StringBuilder("<resources>\n")

        attrs.forEach { sb.append(buildString(it)) }

        sb.append("</resources>\n")
        return sb.toString()
    }

    private fun buildString(attr: Attribute): String {
        val sb = StringBuilder()

        val key = attr.id.replace("-", """_""")
        when (attr.value) {
            is StringValue.SimpleString -> {
                sb.append(buildSimpleString(key, attr.value))
            }
            is StringValue.Plural -> {
                sb.append(buildPlural(key, attr.value))
            }
        }

        return sb.toString()
    }

    private fun buildSimpleString(key: String, str: StringValue.SimpleString): String {
        val escaped = escapeString(str.value)
        return "    <string name=\"${key}\">${escaped}</string>\n"
    }

    private fun buildPlural(key: String, plural: StringValue.Plural): String {
        val sb = StringBuilder("    <plurals name=\"${key}\">\n")

        plural.map.keys.forEach {
            val escaped = escapeString(plural.map[it] ?: return@forEach)
                .replace("{\$${plural.variable}}", "%d")

            when (it) {
                "0" -> sb.append("        <item quantity=\"zero\">$escaped</item>\n")
                "1" -> sb.append("        <item quantity=\"one\">$escaped</item>\n")
                "other" -> sb.append("        <item quantity=\"other\">$escaped</item>\n")
            }
        }

        sb.append("    </plurals>\n")
        return sb.toString()
    }

    private fun escapeString(value: String): String {
        return value
            .replace("'", """\'""")
            .replace("&", """&amp;""")
            .replace("...", """&#8230;""")
    }

    private fun buildStringMap(ftlPath: String = PATH_FTL): Map<String, List<Attribute>> {
        return File(ftlPath).listFiles()
            ?.filter { it.extension == "ftl" }
            ?.map { it.nameWithoutExtension to parseStrings(it) }
            ?.toMap()
            ?: emptyMap()
    }

    private fun parseStrings(file: File): List<Attribute> {
        val input = file.readText()

        //println("================")
        //FluentGrammar().tokenizer.tokenize(input).forEach { println(it) }
        //println("================")
        return FluentGrammar().parseToEnd(input).apply {
            //forEach { println("${it.id} = ${it.value}") }
        }
    }

    companion object {
        private const val PATH_FTL = "./fluent"
    }
}

sealed class StringValue {
    data class SimpleString(val value: String) : StringValue()
    data class Plural(val variable: String, val map: Map<String, String>) : StringValue()
}

data class Attribute(
    val id: String,
    val value: StringValue
)

interface FluentParser {
    fun parseToEnd(file: File): List<Attribute>
}

class SimpleParser : FluentParser {
    private val id = """[a-zA-Z][a-zA-Z0-9\-_]""".toRegex()

    override fun parseToEnd(file: File): List<Attribute> {
        val input = file.readText()

        file.readLines().filter { it.isNotBlank() }.forEachIndexed { index, s ->
            id.matchEntire(s)
        }

        return emptyList()
    }
}

class FluentGrammar : Grammar<List<Attribute>>(), FluentParser {

    private val newLine by token("""\n""")

    private val comment by token("#{1,3}.+")
    private val commentLine by (comment * -newLine)

    private val stringId by token("""\b[a-zA-Z0-9]([a-zA-Z0-9\-_])*\b""")
    private val confirmStringId by stringId * -parser { stringAssign }

    private val blockStart by token("""\{""")
    private val blockEnd by token("}")

    private val arrow by token("""\s*->\s*\n""")
    private val dollar by token("""\$""")

    private val variable
            by (blockStart * dollar * stringId * blockEnd) map {
                it.t1.text + it.t2.text + it.t3.text + it.t4.text
            }
    private val placeholder
            by (blockStart * stringId * blockEnd) map {
                it.t1.text + it.t2.text + it.t3.text
            }

    private val pluralParam by -dollar * stringId * -arrow

    private val pluralIndexTagStart by token(""" {4}\[""")
    private val pluralIndexTagEnd by token("]")

    private val pluralIndexTag by -pluralIndexTagStart * stringId * -pluralIndexTagEnd
    private val pluralOthersTag by token(""" {3}\*\[other] """)
    private val pluralIndexLine by pluralIndexTag * parser { stringBlock }
    private val pluralOtherLine by -pluralOthersTag * parser { stringBlock }
    private val pluralBlock
            by pluralParam * zeroOrMore(pluralIndexLine) * pluralOtherLine

    private val plural by -blockStart * pluralBlock * -blockEnd

    private val words by (stringId or parser { textChar }) map { it.text }

    private val stringWords by oneOrMore(words or variable or placeholder)
    private val stringValue by stringWords map { word ->
        word.joinToString("")
    }
    private val stringLine by stringValue * -newLine
    private val stringLines by zeroOrMore(stringLine)
    private val stringBlock by stringLines map {
        trimLeastCommonSpaces(it).joinToString("")
    }

    private val pluralType
            by plural map {
                val map: Map<String, String> = it.t2.map { it.t1.text to it.t2 }.toMap().toMutableMap().apply { put("other", it.t3) }
                StringValue.Plural(it.t1.text, map)
            }

    private val stringType
            by stringBlock map { StringValue.SimpleString(it) }

    private val attributeValue by pluralType or stringType
    private val attribute by (confirmStringId * attributeValue)
        .map { Attribute(it.t1.text, it.t2) }

    private val stringAssign by token(""" *=\s*""")
    private val textChar by token(""".+""")

    private val discardNewLine by newLine asJust null
    private val discardComment by commentLine asJust null

    private val item by discardNewLine or discardComment or attribute
    private val items by oneOrMore(item) map { it.filterNotNull() }

    override val rootParser by items

    override fun parseToEnd(file: File): List<Attribute> {
        return rootParser.parseToEnd(tokenizer.tokenize(file.readText()))
    }

    private fun trimLeastCommonSpaces(list: List<String>): List<String> {
        var leastCommonSpace = 0
        list.filter { it.leadingSpaces > 0 }.forEach {
            leastCommonSpace = if (leastCommonSpace == 0) {
                it.leadingSpaces
            } else {
                min(leastCommonSpace, it.leadingSpaces)
            }
        }
        return list.map {
            if (leastCommonSpace > 0 && it.leadingSpaces > 0) {
                it.substring(leastCommonSpace)
            } else {
                it
            }
        }
    }

    private val String.leadingSpaces get() = indexOf(trim())
}