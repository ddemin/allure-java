description = "Allure OkHttp3 Integration"

val okhttpVersion = "3.14.9"

dependencies {
    api(project(":allure-attachments"))
    implementation(project(":allure-okhttp3"))
    implementation("com.squareup.retrofit2:retrofit")
    implementation("com.squareup.retrofit2:converter-jackson")
    implementation("com.squareup.retrofit2:converter-scalars")
    testImplementation("com.github.tomakehurst:wiremock")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.jboss.resteasy:resteasy-client")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.mockito:mockito-core")
    testImplementation("org.slf4j:slf4j-simple")
    testImplementation(project(":allure-java-commons-test"))
    testImplementation(project(":allure-junit-platform"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.jar {
    manifest {
        attributes(mapOf(
                "Automatic-Module-Name" to "io.qameta.allure.okhttp3"
        ))
    }
}

tasks.test {
    useJUnitPlatform()
}
