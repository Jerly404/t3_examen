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

        // 👉 Repositorio Mapbox con autenticación
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoicGNyaXNvbG9nb3VwbiIsImEiOiJjbWJlZHA4anExYmJmMmlvZXozN2toc3NxIn0.2hqzXAqHl0X07G871E-plQ"
            }
        }
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()

        // 👉 Repositorio Mapbox también aquí
        maven {
            url = uri("https://api.mapbox.com/downloads/v2/releases/maven")
            credentials {
                username = "mapbox"
                password = "pk.eyJ1IjoicGNyaXNvbG9nb3VwbiIsImEiOiJjbWJlZHA4anExYmJmMmlvZXozN2toc3NxIn0.2hqzXAqHl0X07G871E-plQ"
            }
        }
    }
}

rootProject.name = "T3_Examen"
include(":app")
