package net.crosier.sonar

import org.xqdoc.XQueryParser
import org.xqdoc.XQueryParserBaseListener

data class Violation(val lineNumber : Int)

abstract class AbstractXQueryParserListener : XQueryParserBaseListener() {
    internal val violations = mutableListOf<Violation>()
    protected fun addViolation(violation: Violation) = violations.add(violation)
}

class XdmpEvalListener : AbstractXQueryParserListener() {
    override fun enterQName(ctx: XQueryParser.QNameContext) {
        val qname = ctx?.FullQName()?.text ?: ""

        if (qname.toLowerCase() == "xdmp:eval") {
            addViolation(Violation(ctx!!.FullQName().symbol.line))
        }
    }
}