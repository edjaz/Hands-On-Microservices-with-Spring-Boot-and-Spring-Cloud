package fr.edjaz.api.event

import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test

class EventTest {
    @Test
    fun `shouldCreate`() {
        var event = Event(Event.Type.CREATE, "key", "data")
        assertNotNull(event)
        assertEquals("data", event.data)
        assertEquals("key", event.key)
        assertEquals(Event.Type.CREATE, event.eventType)
    }
}
