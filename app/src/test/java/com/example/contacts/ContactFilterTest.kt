package com.example.contacts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ContactFilterTest {
    private val contacts = listOf(
        Contact(1, "Ben Ali", "Ahmed", "22123456", "a@x.tn"),
        Contact(2, "Trabelsi", "Sarra", "98123456", "s@x.tn"),
        Contact(3, "Gharbi", "Élodie", "55 123 456", "e@x.tn")
    )

    @Test fun emptyQueryReturnsAll() = assertEquals(3, ContactFilter.filter(contacts, "  ").size)

    @Test fun filtersByLastNameIgnoringCase() =
        assertEquals(listOf(2), ContactFilter.filter(contacts, "TRAB").map { it.id })

    @Test fun filtersByFirstName() =
        assertEquals(listOf(1), ContactFilter.filter(contacts, "ahm").map { it.id })

    @Test fun filtersByPhone() =
        assertEquals(listOf(2), ContactFilter.filter(contacts, "9812").map { it.id })

    @Test fun ignoresAccents() =
        assertEquals(listOf(3), ContactFilter.filter(contacts, "elodie").map { it.id })

    @Test fun noResultGivesEmptyList() = assertTrue(ContactFilter.filter(contacts, "zzz").isEmpty())

    @Test fun filteredContactKeepsItsOwnId() =
        assertEquals(2, ContactFilter.filter(contacts, "sarra").first().id)
}
