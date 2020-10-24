package net.crosier.sonar

import org.sonar.api.Plugin
import org.sonar.api.Plugin.Context
import org.sonar.api.resources.AbstractLanguage
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition

class XQueryPlugin : Plugin {

    override fun define(context: Context) {
        context.addExtensions(
                XQuery::class.java,
                XQueryDefaultProfile::class.java,
                XQuerySensor::class.java,
                XQueryRules::class.java
        )
    }
}

class XQuery : AbstractLanguage(KEY, LABEL) {

    override fun getFileSuffixes() = arrayOf(".xqy",".xq",".xql",".xqm",".xquery")

    companion object {
        internal const val KEY = "xquery"
        internal const val LABEL = "XQuery"
    }

}
