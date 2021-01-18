package com.marklogic.sonar

import org.antlr.v4.runtime.Token
import org.antlr.v4.runtime.tree.Trees
import org.xqdoc.XQueryParser
import org.xqdoc.XQueryParserBaseListener

data class Violation(val lineNumber : Int)

abstract class AbstractXQueryParserListener : XQueryParserBaseListener() {
    internal val violations = mutableListOf<Violation>()
    protected fun addViolation(violation: Violation) = violations.add(violation)
}

/**
 * Checks main modules, and sections of main modules for xquery version.
 * Flags usage of older marklogic based dialect
 */
class XQueryVersionListener : AbstractXQueryParserListener() {

    override fun enterModule(ctx: XQueryParser.ModuleContext?) {
        super.enterModule(ctx)

        if (ctx?.mainModule()?.isNotEmpty() == true) {
            var hasVersion = false
            var lineNumber = 1
            for (child in ctx.children) {
                when (child.payload) {
                    is XQueryParser.VersionDeclContext -> {
                        if ("\"0.9-ml\"" == (child.payload as XQueryParser.VersionDeclContext).version?.text) {
                            addViolation(Violation(if (lineNumber == 1) 1 else lineNumber + 1))
                        }
                        hasVersion = true
                    }
                    is XQueryParser.MainModuleContext -> {
                        if (!hasVersion) {
                            addViolation(Violation(lineNumber))
                        }
                        hasVersion = false
                    }
                    is Token -> {
                        lineNumber = (child.payload as Token).line
                    }
                }
            }
        } else {  // in library module - including quotes due to parsing rules, and since it is always quoted
            if (ctx?.versionDecl()?.isEmpty() == true || "\"0.9-ml\"" == ctx?.versionDecl()?.get(0)?.version?.text) {
                addViolation(Violation(1))
            }
        }
    }
}

class StrongTypingInFunctionDeclarationListener : AbstractXQueryParserListener() {
    override fun enterFunctionParam(ctx: XQueryParser.FunctionParamContext?) {
        super.enterFunctionParam(ctx)

        if (ctx?.typeDeclaration() == null) {
            addViolation(Violation(ctx!!.DOLLAR().symbol.line))
        }
    }
}

class StrongTypingInModuleVariableListener : AbstractXQueryParserListener() {
    override fun enterVarDecl(ctx: XQueryParser.VarDeclContext?) {
        super.enterVarDecl(ctx)

        if (ctx?.typeDeclaration() == null) {
            addViolation(Violation(ctx!!.KW_VARIABLE().symbol.line))
        }
    }
}

class XdmpEvalListener : AbstractXQueryParserListener() {
    override fun enterQName(ctx: XQueryParser.QNameContext) {
        val qname = ctx.FullQName()?.text ?: ""

        if (qname.toLowerCase() == "xdmp:eval") {
            addViolation(Violation(ctx.FullQName().symbol.line))
        }
    }
}

class XdmpValueListener : AbstractXQueryParserListener() {
    override fun enterQName(ctx: XQueryParser.QNameContext) {
        val qname = ctx.FullQName()?.text ?: ""

        if (qname.toLowerCase() == "xdmp:value") {
            addViolation(Violation(ctx.FullQName().symbol.line))
        }
    }
}

class OperationsInPredicateListener : AbstractXQueryParserListener() {
    private val functionNames = arrayOf("data", "last", "not", "exists", "xs:integer", "string", "xs:decimal", "xs:double", "xs:float", "xs:date", "xs:dateTime", "xs:time", "xs:dayTimeDuration", "xs:yearMonthDuration", "xs:duration")
    private val expressions = arrayOf(XQueryParser.PLUS, XQueryParser.MINUS, XQueryParser.KW_DIV, XQueryParser.MOD, XQueryParser.STAR)

    override fun enterPredicate(ctx: XQueryParser.PredicateContext?) {
        super.enterPredicate(ctx)

        /* Check Expresions */
        for (expr in expressions) {
            if (Trees.findAllTokenNodes(ctx, expr).isNotEmpty()) {
                addViolation(Violation(ctx!!.LBRACKET().symbol.line))
            }
        }

        /**
         * Check for function calls
         * In theory, we could just report *ALL* function calls, instead of the ones listed in the array above.
         * In fact, that might be better, but currently reimplementing rules from previous edition.
         * Unfortunately, if the user passes in fn:string() instead of just string(), this will *NOT* currently be caught.
         **/
        for (fn in Trees.findAllRuleNodes(ctx, XQueryParser.RULE_functionCall)) {
            /* Fully Qualified Names */
            for (fqn in Trees.findAllTokenNodes(fn, XQueryParser.FullQName)) {
                if (functionNames.contains(fqn.text)) {
                    addViolation(Violation(ctx!!.LBRACKET().symbol.line))
                }
            }

            /* not fully qualified names */
            for (ncn in Trees.findAllTokenNodes(fn, XQueryParser.NCName)) {
                if (functionNames.contains(ncn.text)) {
                    addViolation(Violation(ctx!!.LBRACKET().symbol.line))
                }
            }
        }
    }
}

class StrongTypingInFlworListener : AbstractXQueryParserListener() {
    override fun enterLetClause(ctx: XQueryParser.LetClauseContext?) {
        super.enterLetClause(ctx)

        // allowing bypass of $_ variable name from strong typing
        if (ctx?.letBinding?.typeDeclaration() == null && !(ctx!!.letBinding.varName().text == ("_"))) {
            addViolation(Violation(ctx.KW_LET().symbol.line))
        }
    }

    override fun enterForClause(ctx: XQueryParser.ForClauseContext?) {
        super.enterForClause(ctx)

        if (ctx?.forBinding()?.size ?:0 > 0)
            for (binding in ctx!!.forBinding()) {
                if (binding?.typeDeclaration() == null) {
                    addViolation(Violation(ctx.KW_FOR().symbol.line))
                }
            }
    }
}

class FunctionMappingListener : AbstractXQueryParserListener() {
    override fun enterModule(ctx: XQueryParser.ModuleContext?) {
        super.enterModule(ctx)

        if (ctx!!.childCount > 0) {
            var capable = false
            var lineNumber = 1
            for (child in ctx.children) {
                when (child.payload) {
                    is XQueryParser.VersionDeclContext -> {
                        if ("\"1.0-ml\"" == (child.payload as XQueryParser.VersionDeclContext).version?.text) {
                            capable = true
                        }
                    }
                    is XQueryParser.MainModuleContext -> {
                        val mmCtx = (child.payload as XQueryParser.MainModuleContext)
                        if (capable && mmCtx.prolog() != null) {
                            checkProlog(mmCtx.prolog(), if (lineNumber == 1) 1 else lineNumber + 1)
                        }
                        capable = false // reset for next main module, needs new xquery version node to set capable
                    }
                    is XQueryParser.LibraryModuleContext -> {
                        val lmCtx = (child.payload as XQueryParser.LibraryModuleContext)
                        if (capable && lmCtx.prolog() != null) {
                            checkProlog(lmCtx.prolog(), lineNumber)
                        }
                    }
                    is Token -> lineNumber = (child.payload as Token).line
                }
            }
        }
    }

    private fun checkProlog(ctx: XQueryParser.PrologContext, line: Int) {
        for(option in ctx.optionDecl()) {
            if ("xdmp:mapping" == option.name.text) return
        }
        addViolation(Violation(line))
    }
}

class XPathDescendantStepsListener : AbstractXQueryParserListener() {
    override fun enterRelativePathExpr(ctx: XQueryParser.RelativePathExprContext?) {
        super.enterRelativePathExpr(ctx)

        if (ctx!!.DSLASH().size > 0) {
            addViolation(Violation(ctx.start.line))
        }
    }

    override fun enterPathExpr(ctx: XQueryParser.PathExprContext?) {
        super.enterPathExpr(ctx)

        if (ctx!!.DSLASH() != null) {
            addViolation(Violation(ctx.start.line))
        }
    }
}

class XPathTextStepsListener : AbstractXQueryParserListener() {
    override fun enterFunctionCall(ctx: XQueryParser.FunctionCallContext?) {
        super.enterFunctionCall(ctx)

        if (ctx!!.eqName().text == "text") {
            addViolation(Violation(ctx.start.line))
        }
    }
}

/** TODO: EffectiveBooleanListener
 * Implementation on hold.  Requires searching entire project for other function calls/return types, which this
 * version does not support due to memory constraints.
 **/