package net.crosier.sonar

import net.crosier.sonar.XQuery.Companion.KEY
import net.crosier.sonar.XQueryChecks.Companion.REPOSITORY_KEY
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
}