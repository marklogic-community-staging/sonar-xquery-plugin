package com.marklogic.sonar

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

class XQueryDefaultProfile : BuiltInQualityProfilesDefinition {
    override fun define(context: BuiltInQualityProfilesDefinition.Context) {
        val default = context.createBuiltInQualityProfile("Default Profile", XQuery.KEY)

        for (check in XQueryChecks.defaultChecks) {
            default.activateRule(XQueryChecks.REPOSITORY_KEY, check.key)
        }
    }
}

class XQuerySonarWayProfile : BuiltInQualityProfilesDefinition {
    override fun define(context: BuiltInQualityProfilesDefinition.Context) {
        val sonarWay = context.createBuiltInQualityProfile("Sonar Way", XQuery.KEY)

        for (check in XQueryChecks.checks) {
            sonarWay.activateRule(XQueryChecks.REPOSITORY_KEY, check.key)
        }

        sonarWay.done()
    }
}