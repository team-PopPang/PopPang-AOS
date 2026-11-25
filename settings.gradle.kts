pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            name = "PopPangReactNative"
            url = uri("$rootDir/Vendor/PopPangRN/repository")
        }
        val rnPackagesUser = providers.gradleProperty("gpr.user")
            .orElse(providers.environmentVariable("GITHUB_ACTOR")).orNull
        val rnPackagesKey = providers.gradleProperty("gpr.key")
            .orElse(providers.environmentVariable("GITHUB_TOKEN")).orNull
        if (!rnPackagesUser.isNullOrBlank() && !rnPackagesKey.isNullOrBlank()) {
            maven {
                name = "PopPangReactNativePackages"
                url = uri("https://maven.pkg.github.com/team-PopPang/PopPang-RN")
                credentials {
                    username = rnPackagesUser
                    password = rnPackagesKey
                }
                content { includeGroupByRegex("com\\.poppang(\\..*)?") }
            }
        }
        maven("https://devrepo.kakao.com/nexus/content/groups/public/")
        maven("https://repository.map.naver.com/archive/maven")
    }
}

rootProject.name = "PopPang-AOS"
include(":app")
