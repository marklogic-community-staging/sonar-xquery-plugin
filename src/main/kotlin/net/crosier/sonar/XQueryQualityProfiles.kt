package net.crosier.sonar

import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

class XQueryDefaultProfile : BuiltInQualityProfilesDefinition {
    override fun define(context: BuiltInQualityProfilesDefinition.Context) {
        val default = context.createBuiltInQualityProfile("Default Profile", XQuery.KEY)

        //Add Default Checks
    }
}