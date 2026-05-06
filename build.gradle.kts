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
    }
}
