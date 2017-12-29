package com.beust.klaxon

import org.testng.Assert
import org.testng.annotations.Test
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
annotation class KlaxonDate
annotation class KlaxonDayOfTheWeek

@Test
class BindingAdapterTest {
    class WithDate @JvmOverloads constructor(
            @Json(name = "theDate")
            @field:KlaxonDate
            var date: LocalDateTime? = null,

            @field:KlaxonDayOfTheWeek
            var dayOfTheWeek: String? = null // 0 = Sunday, 1 = Monday, ...
    )

    fun fieldAdapters() {
        val result = Klaxon()
                .fieldConverter(KlaxonDate::class, object: Converter {
                    override fun fromJson(jv: JsonValue)
                            = LocalDateTime.parse(jv.string,
                            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))

                    override fun toJson(o: Any): String {
                        return """ {
                    | "date" : ${o?.toString()} }
                    | """.trimMargin()
                    }
                })

                .fieldConverter(KlaxonDayOfTheWeek::class, object: Converter {
//                override fun canConvert(field: KProperty<*>?, value: Any): Boolean {
//                    return field?.returnType == String::class
//                            || value::class == String::class
//                }

                    override fun fromJson(jv: JsonValue) : String {
                        return when(jv.int) {
                            0 -> "Sunday"
                            1 -> "Monday"
                            2 -> "Tuesday"
                            else -> "Some other day"
                        }
                    }

                    override fun toJson(o: Any) : String {
                        return when(o) {
                            "Sunday" -> "0"
                            "Monday" -> "1"
                            "Tuesday" -> "2"
                            else -> "-1"
                        }
                    }
                })
                .parse<WithDate>("""
                {
                  "theDate": "2017-05-10 16:30"
                  "dayOfTheWeek": 2
                }
            """)
        Assert.assertEquals(result?.dayOfTheWeek, "Tuesday")
        Assert.assertEquals(result?.date, LocalDateTime.of(2017, 5, 10, 16, 30))
    }

    val CARD_ADAPTER = object: Converter {

        override fun fromJson(value: JsonValue): Card? {
            fun parseCard(str: String) : Card? {
                val s0 = str[0]
                val cardValue =
                        if (s0 == '1' && str[1] == '0') 10
                        else if (s0 == 'K') 13
                        else (s0 - '0')
                val suit = when(str[1]) {
                    'H' -> "Hearts"
                    'S' -> "Spades"
                    else -> ""
                }
                return if (suit != "") Card(cardValue, suit) else null
            }
            val result =
                    if (value.string != null) {
                        val str = value.string
                        if (str != null) parseCard(str) else null
                    } else {
                        null
                    }
            return result
        }

        override fun toJson(obj: Any): String {
            return "some JSON"
        }
    }

    private fun privateConverter2(withAdapter: Boolean) {
        val klaxon = Klaxon()
        if (withAdapter) klaxon.converter(CARD_ADAPTER)
        val result = klaxon.parse<Deck1>("""
            {
                "cardCount": 1,
                "card" : "KS"
            }
        """)
        Assert.assertEquals(result?.cardCount, 1)
        Assert.assertEquals(result?.card, Card(13, "Spades"))
    }

    fun withConverter2() = privateConverter2(withAdapter = true)

    @Test(expectedExceptions = arrayOf(KlaxonException::class))
    fun withoutConverter2() = privateConverter2(withAdapter = false)
}