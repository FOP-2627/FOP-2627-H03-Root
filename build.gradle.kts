plugins {
    alias(libs.plugins.algomate)
    alias(libs.plugins.style)
}

version = file("version").readLines().first()

submission {
    // ACHTUNG!
    // Setzen Sie im folgenden Bereich Ihre TU-ID (NICHT Ihre Matrikelnummer!), Ihren Nachnamen und Ihren Vornamen
    // in Anführungszeichen (z.B. "ab12cdef" für Ihre TU-ID) ein!
    // BEISPIEL:
    // studentId = "ab12cdef"
    // firstName = "sol_first"
    // lastName = "sol_last"
    studentId = "ab12cdef"
    firstName = "sol_first"
    lastName = "sol_last"

    // Optionally require own tests for mainBuildSubmission task. Default is false
    requireTests = false
}

dependencies {
    implementation(libs.fopbot)
}

jagr {
    graders {
        val graderPublic by creating {
            rubricProviderName.set("h03.H03_RubricProviderPublic")
        }
        val graderPrivate by getting {
            parent(graderPublic)
            graderName.set("FOP-2627-H03-Private")
            rubricProviderName.set("h03.H03_RubricProviderPrivate")
        }
        val templateAnnotation by creating {
            configureDependencies {
                implementation("org.tudalgo:algoutils-tutor:0.9.0")
            }
        }
    }
}

val jsonTask = tasks.register<JavaExec>("generateJSON") {
    group = "verification"
    description = "(re-)generates randomized json files for the parameterized tests"
    workingDir = File(".") // if unset, files will be put into build dir
    mainClass.set("h03.RandomizedJsonGenerator")
}

// build fails if this is not done after evaluation
afterEvaluate {
    jsonTask.configure {
        classpath = sourceSets["graderPrivate"].runtimeClasspath
        dependsOn(tasks.named("graderPrivateClasses"))
    }
}
