pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

include(":dialer:app")
include(":dialer:resources")
include(":dialer:common")

include(":protos")

include(":quantum")

include(":product")

include(":incallui:resources")

include(":bubble")

include(":voicemail")
