package com.marklogic.sonar

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
                XQueryVersionCheck(),
                StrongTypingInFunctionDeclarationCheck(),
                StrongTypingInModuleVariableCheck(),
                XdmpEvalCheck(),
                XdmpValueCheck(),
                OperationsInPredicateCheck(),
                StrongTypingInFlworCheck(),
                FunctionMappingCheck(),
                XPathDescendantStepsCheck(),
                XPathTextStepsCheck()
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

/* ****************** */
/* CRITICAL CHECKS    */
/* ****************** */
class XQueryVersionCheck : AbstractXQueryCheck<XQueryVersionListener>(XQueryVersionListener::class) {
    override val key = "xquery-version"
    override val name = "XQuery Version"
    override val description = "Ensure that you declare the latest XQuery version (1.0-ml/3.0) at the top of each of your scripts (as opposed to declaring an older version - 0.9-ml - or not declaring a version at all). This ensures better compatibility of code after server upgrades and consistent behavior in XQuery processing."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.CRITICAL
}

class StrongTypingInFunctionDeclarationCheck : AbstractXQueryCheck<StrongTypingInFunctionDeclarationListener>(StrongTypingInFunctionDeclarationListener::class) {
    override val key = "strong-typing-function-declaration"
    override val name = "Use Strong Typing in Function Declarations"
    override val description = "Declare types for function parameters and return types to increase readability and catch potential bugs. Also try to scope the types as narrowly as possible (i.e. use 'element()' instead of 'item()' when returning an element) and include quantifiers on each type."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.CRITICAL
}

class StrongTypingInModuleVariableCheck : AbstractXQueryCheck<StrongTypingInModuleVariableListener>(StrongTypingInModuleVariableListener::class) {
    override val key = "strong-typing-module-variable"
    override val name = "Use Strong Typing when Declaring Module Variable"
    override val description = "Declare types for declared variables to increase readability and catch potential bugs. Also try to scope the types as narrowly as possible (i.e. use 'element()' instead of 'item()' when the value is an element) and include quantifiers on each type."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.CRITICAL
}

/* ****************** */
/* MAJOR CHECKS       */
/* ****************** */
class XdmpEvalCheck : AbstractXQueryCheck<XdmpEvalListener>(XdmpEvalListener::class) {
    override val key = "xdmp-eval"
    override val name = "xdmp:eval should be avoided where possible."
    override val description = "xdmp:eval is a threat of code injection, and should be avoided."
    override val type = RuleType.VULNERABILITY
    override val message = "Avoid using xdmp:eval() where possible. Instead use either xdmp:invoke(), xdmp:unpath() or if possible assign functions to variables to dynamically evaluate code logic"
    override val severity = Severity.MAJOR
}

class XdmpValueCheck : AbstractXQueryCheck<XdmpValueListener>(XdmpValueListener::class) {
    override val key = "xdmp-value"
    override val name = "xdmp:value should be avoided where possible."
    override val description = "xdmp:value can potentially expose an injection vulernability, and should be avoided when using externally provided values"
    override val type = RuleType.VULNERABILITY
    override val message = "Avoid using xdmp:value() where possible. Instead use either xdmp:invoke(), xdmp:unpath() or if possible assign functions to variables to dynamically evaluate code logic"
    override val severity = Severity.MINOR
}

class OperationsInPredicateCheck : AbstractXQueryCheck<OperationsInPredicateListener>(OperationsInPredicateListener::class) {
    override val key = "operations-in-predicate"
    override val name = "Avoid Operations in Predicates"
    override val description = "Instead of calling functions or performing operations in predicates try assigning the results to a variable before the predicate."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MAJOR
}

/* ****************** */
/* MINOR CHECKS       */
/* ****************** */
class StrongTypingInFlworCheck : AbstractXQueryCheck<StrongTypingInFlworListener>(StrongTypingInFlworListener::class) {
    override val key = "strong-typing-in-FLWOR"
    override val name = "Use Strong Typing FLWOR Expressions"
    override val description = "Declare types for FLWOR 'let' and 'for' clauses to increase readability and catch potential bugs. Also try to scope the types as narrowly as possible (i.e. use 'element()' instead of 'item()' when the value is an element) and include quantifiers on each type."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MINOR
}

class FunctionMappingCheck : AbstractXQueryCheck<FunctionMappingListener>(FunctionMappingListener::class) {
    override val key = "function-mapping"
    override val name = "Function Mapping Usage (MarkLogic)"
    override val description = "Make sure you are intentionally using and/or understand function mapping - otherwise disable it with 'declare option xdmp:mapping \"false\";'. If you wish to use it you should explicitly declare 'declare option xdmp:mapping \"true\";' for readability/maintainability. Please note that this check is Marklogic specific."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MINOR
}

class XPathDescendantStepsCheck : AbstractXQueryCheck<XPathDescendantStepsListener>(XPathDescendantStepsListener::class) {
    override val key = "xpath-descendant-steps"
    override val name = "Avoid Using '//' in XPath"
    override val description = "Favor fully-qualified paths in XPath for readability and to avoid potential performance problems."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MINOR
}

class XPathTextStepsCheck : AbstractXQueryCheck<XPathTextStepsListener>(XPathTextStepsListener::class) {
    override val key = "xpath-text-steps"
    override val name = "Avoid Using text() in XPath"
    override val description = "Generally avoid using /text() in your XPath in favor of using fn:string() or allowing atomization (through strong typing or default atomization)."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MINOR
}

/* TODO: Need to add ability to check entire project functions for effective booleans.
class EffectiveBooleanCheck : AbstractXQueryCheck<EffectiveBooleanListener>(EffectiveBooleanListener::class) {
    override val key = "effective-boolean"
    override val name = "Effective Boolean in Conditional Predicate"
    override val description = "Unless the value in the conditional is of type xs:boolean it is recommended you use fn:exists(), fn:empty(), or other boolean functions inside of conditional predicates to check values."
    override val type = RuleType.CODE_SMELL
    override val message = description
override val severity = Severity.MINOR
}
*/
