package com.beust.klaxon

import org.testng.annotations.Test
import java.time.Instant

class Issue84 {
    /**
     * This test passes.
     */
    @Test
    fun serializeNestedInstant() {
        data class Person(val firstName: String, val dob: Instant)

        class EpochMilliInstantConverter: Converter<Instant> {
            override fun toJson(value: Instant) = value.toEpochMilli().toString()
            override fun fromJson(jv: JsonValue) = throw NotImplementedError()
        }

        // No custom converter, should be the toString() of the instant
        val obj = Person("John", Instant.ofEpochMilli(9001))
        val result = Klaxon().toJsonString(obj)

        // Custom converter, expect the converted value
        val mapper = Klaxon().converter(EpochMilliInstantConverter())
        BindingTest.assertContains(mapper.toJsonString(obj), "9001")
        // Actual: {"dob" : 9001, "firstName" : "John"}
    }

    /**
     * This test does not pass.
     */
    @Test
    fun serializeListOfInstants() {
        val dates = listOf(Instant.ofEpochMilli(9001))

        class EpochMilliInstantConverter: Converter<Instant> {
            override fun toJson(value: Instant) = value.toEpochMilli().toString()
            override fun fromJson(jv: JsonValue) = throw NotImplementedError()
        }

        // despite custom converter being provided, instant is not serialized.  Empty value in list
        val mapper = Klaxon().converter(EpochMilliInstantConverter())
        val result = mapper.toJsonString(dates)
        BindingTest.assertContains(result, "9001")
        // Actual: [{}]
    }
}