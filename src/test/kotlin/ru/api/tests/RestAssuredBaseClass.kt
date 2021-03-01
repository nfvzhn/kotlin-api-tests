package ru.api.tests

import io.restassured.RestAssured
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.LogConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class RestAssuredBaseClass {

    lateinit var requestSpecification: RequestSpecification

    @BeforeAll
    internal fun setUp() {

        val restAssuredLoggingConfig = LogConfig.logConfig().enablePrettyPrinting(true)
        val restAssuredConfig = RestAssuredConfig.config().logConfig(restAssuredLoggingConfig)

        requestSpecification = RequestSpecBuilder()
            .setBaseUri("https://jsonplaceholder.typicode.com")
            .setBasePath("/")
            .setContentType(ContentType.JSON)
            .setRelaxedHTTPSValidation()
            .setConfig(restAssuredConfig)
            .build()
    }

    @AfterAll
    internal fun tearDown() {
        RestAssured.reset()
    }
}
