package fr.edjaz.microservices.composite.product

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import fr.edjaz.api.core.product.Product
import fr.edjaz.api.event.Event
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test

class IsSameEventTests {
    var mapper = ObjectMapper()

    @Test
    @Throws(JsonProcessingException::class)
    fun testEventObjectCompare() {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        val event1 = Event(Event.Type.CREATE, 1, Product(1, "name", 1, null))
        val event2 = Event(Event.Type.CREATE, 1, Product(1, "name", 1, null))
        val event3 = Event<Int, Product>(Event.Type.DELETE, 1, null)
        val event4 = Event(Event.Type.CREATE, 1, Product(2, "name", 1, null))
        val event1JSon = mapper.writeValueAsString(event1)
        MatcherAssert.assertThat(event1JSon, Matchers.`is`(IsSameEvent.Companion.sameEventExceptCreatedAt(event2)))
        MatcherAssert.assertThat(event1JSon, Matchers.not(IsSameEvent.Companion.sameEventExceptCreatedAt(event3)))
        MatcherAssert.assertThat(event1JSon, Matchers.not(IsSameEvent.Companion.sameEventExceptCreatedAt(event4)))
    }
}
