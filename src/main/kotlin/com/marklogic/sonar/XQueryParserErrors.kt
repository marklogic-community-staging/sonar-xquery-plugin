package com.marklogic.sonar

import org.antlr.v4.runtime.BaseErrorListener
import org.antlr.v4.runtime.RecognitionException
import org.antlr.v4.runtime.Recognizer
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType

data class Error(val lineNumber: Int, val message: String)

class XQueryErrorListener : BaseErrorListener() {
    val key  = "XQueryParseErrors"
    val name = "XQuery Parser Errors"
    val htmlDescription = "Parse Errors were found during the process of creating the ParseTree for this file. This means there is either an error in the file near the given line, or the syntax used is not known by the XQuery Grammer in use."
    val type = RuleType.BUG
    val severity = Severity.INFO

    internal val errors = mutableListOf<Error>()
    protected fun addError(error: Error) = errors.add(error)

    override fun syntaxError(recognizer: Recognizer<*, *>?, offendingSymbol: Any?, line: Int, charPositionInLine: Int, msg: String?, e: RecognitionException?) {
        addError(Error(line, msg ?: "No Message Provided" ))
        super.syntaxError(recognizer, offendingSymbol, line, charPositionInLine, msg, e)
    }
}