package net.crosier.sonar

import org.antlr.v4.runtime.tree.ParseTree
import org.antlr.v4.runtime.tree.ParseTreeWalker
import org.sonar.api.rule.Severity
import org.sonar.api.rules.RuleType
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor



class XQueryChecks {
    companion object {
        const val REPOSITORY_KEY = "xquery"
        const val PROFILE_NAME = "Default"
        val checks = arrayOf(
                XdmpEvalCheck(),
                XdmpValueCheck()
        )
    }
}

interface XQueryCheck {
    val key: String
    val name: String
    val description: String
    val type: RuleType
    val message: String
    val severity: String
}

abstract class AbstractXQueryCheck<L : AbstractXQueryParserListener>(private val parserListerClass: KClass<L>) : XQueryCheck {
    internal fun violations(tree: ParseTree): List<Violation> {
        val listener = parserListerClass.primaryConstructor!!.call()
        ParseTreeWalker().walk(listener,tree)
        return listener.violations
    }
}

class XdmpEvalCheck : AbstractXQueryCheck<XdmpEvalListener>(XdmpEvalListener::class) {
    override val key = "xdmpeval"
    override val name = "xdmp:eval should be avoided where possible"
    override val description = "xdmp:eval is a threat of code injection, and should be avoided."
    override val type = RuleType.VULNERABILITY
    override val message = "Avoid using xdmp:eval() where possible. Instead use either xdmp:invoke(), xdmp:unpath() or if possible assign functions to variables to dynamically evaluate code logic"
    override val severity = Severity.MAJOR
}

class XdmpValueCheck : AbstractXQueryCheck<XdmpEvalListener>(XdmpEvalListener::class) {
    override val key = "xdmpvalue"
    override val name = "xdmp:eval should be avoided where possible"
    override val description = "xdmp:eval is a threat of code injection, and should be avoided."
    override val type = RuleType.VULNERABILITY
    override val message = "Avoid using xdmp:eval() where possible. Instead use either xdmp:invoke(), xdmp:unpath() or if possible assign functions to variables to dynamically evaluate code logic"
    override val severity = Severity.MAJOR
}