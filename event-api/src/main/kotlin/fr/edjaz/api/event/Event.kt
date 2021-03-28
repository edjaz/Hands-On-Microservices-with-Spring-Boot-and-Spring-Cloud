package fr.edjaz.api.event

import java.time.LocalDateTime

class Event<K, T> {

    enum class Type {
        CREATE, DELETE
    }

    var eventType: Type?
        private set
    var key: K?
        private set
    var data: T?
        private set
    var eventCreatedAt: LocalDateTime?
        private set

    constructor() {
        eventType = null
        key = null
        data = null
        eventCreatedAt = null
    }

    constructor(eventType: Type, key: K, data: T?) {
        this.eventType = eventType
        this.key = key
        this.data = data
        eventCreatedAt = LocalDateTime.now()
    }

    override fun toString(): String {
        return "Event(eventType=$eventType, key=$key, data=$data, eventCreatedAt=$eventCreatedAt)"
    }


}
