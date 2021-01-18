package com.marklogic.sonar

import com.marklogic.sonar.XQuery.Companion.KEY
import com.marklogic.sonar.XQueryChecks.Companion.REPOSITORY_KEY
import com.marklogic.sonar.XQueryErrorListener
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.sonar.api.batch.fs.FileSystem
import org.sonar.api.batch.fs.InputFile
import org.sonar.api.batch.fs.InputFile.Type.MAIN
import org.sonar.api.batch.fs.InputFile.Type.TEST
import org.sonar.api.batch.sensor.Sensor
import org.sonar.api.batch.sensor.SensorContext
import org.sonar.api.batch.sensor.SensorDescriptor
import org.sonar.api.rule.RuleKey
import org.xqdoc.XQueryLexer
import org.xqdoc.XQueryParser

class XQuerySensor(private val fs: FileSystem) : Sensor {

    val sources: Iterable<InputFile>
        get() = fs.inputFiles(MAIN)

    val tests: Iterable<InputFile>
        get() = fs.inputFiles(TEST)

    override fun describe(descriptor: SensorDescriptor) {
        descriptor.onlyOnLanguage(KEY).name("XQuerySensor")
    }

    override fun execute(context: SensorContext) {
        sources.forEach { inputFile: InputFile ->
            val stream = CharStreams.fromStream(inputFile.inputStream())
            val lexer = XQueryLexer(stream)
            val tokens = CommonTokenStream(lexer)
            val parser = XQueryParser(tokens)
            val errorListener = XQueryErrorListener()
            parser.addErrorListener(errorListener)
            val parseTree = parser.module()

            errorListener.errors.forEach {(lineNumber, message)  ->
                with(context.newIssue().forRule(RuleKey.of(REPOSITORY_KEY, errorListener.key))) {
                    val location = newLocation().apply {
                        on(inputFile)
                        message(message)
                        at(inputFile.selectLine(lineNumber))
                    }
                    at(location).save()
                }
            }

            XQueryChecks.checks.forEach { check ->
                val violations = check.violations(parseTree)
                violations.forEach { (lineNumber) ->
                    with(context.newIssue().forRule(check.ruleKey())) {
                        val location = newLocation().apply {
                            on(inputFile)
                            message(check.message)
                            at(inputFile.selectLine(lineNumber))
                        }
                        at(location).save()
                    }
                }
            }
        }
    }

    private fun <L : AbstractXQueryParserListener> AbstractXQueryCheck<L>.ruleKey() = RuleKey.of(REPOSITORY_KEY, key)

    private fun FileSystem.inputFiles(type: InputFile.Type): MutableIterable<InputFile> = with(predicates()) {
        return inputFiles(this.and(hasLanguage(KEY), hasType(type)))
    }
}