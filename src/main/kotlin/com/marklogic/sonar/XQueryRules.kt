package com.marklogic.sonar

import com.marklogic.sonar.XQuery.Companion.KEY
import com.marklogic.sonar.XQueryChecks.Companion.REPOSITORY_KEY
import com.marklogic.sonar.XQueryErrorListener
import org.sonar.api.server.rule.RulesDefinition
import org.sonar.api.server.rule.RulesDefinition.Context
import org.sonar.api.server.rule.RulesDefinition.NewExtendedRepository


class XQueryRules : RulesDefinition {
    override fun define(context: Context) {
        context.createRepository(REPOSITORY_KEY, KEY).apply {
            setName("SonarXQueryAnalyzer")
            XQueryChecks.checks.forEach {
                createRule(it)
            }
            // add error listener rule?
            done()
        }

    }

    private fun NewExtendedRepository.createRule(check: XQueryCheck) = createRule(check.key).apply {
        with(check) {
            setName(name)
            setHtmlDescription(description)
            setType(type)
            setSeverity(severity)
        }
    }

    private fun NewExtendedRepository.createRule(listener: XQueryErrorListener) = createRule(listener.key).apply {
        with(listener) {
            setName(name)
            setHtmlDescription(htmlDescription)
            setType(type)
            setSeverity(severity)
        }
    }
}