// Plugin management for the entire project. This defines the repositories where Gradle looks for plugins.
pluginManagement {
    repositories {
        // Google's repository for Android-related plugins and libraries
        google {
            content {
                // Only include groups matching Android-related libraries
                includeGroupByRegex("com\\.android.*")  // Android build tools and libraries
                includeGroupByRegex("com\\.google.*")   // Google-related libraries (e.g., Firebase, Material)
                includeGroupByRegex("androidx.*")       // AndroidX libraries (modern Android libraries)
            }
        }
        // Maven Central for general-purpose Java/Kotlin libraries
        mavenCentral()
        // Gradle Plugin Portal for Gradle-specific plugins
        gradlePluginPortal()
    }
}

// Dependency resolution management for the entire project.
dependencyResolutionManagement {
    // Set mode to fail on repositories declared in individual project modules
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // Google's Maven repository for Android dependencies
        google()
        // Maven Central for other third-party libraries
        mavenCentral()
    }
}

// Set the name of the root project
rootProject.name = "Diashield"

// Include the 'app' module in the project
include(":app")
