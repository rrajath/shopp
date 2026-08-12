package com.rrajath.shopp.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class Uuidv7Test {

    @Test
    fun `1000 ids generated in a tight loop are strictly increasing as strings`() {
        val ids = (1..1000).map { Uuidv7.generate() }
        for (i in 1 until ids.size) {
            assertTrue(
                "expected ${ids[i - 1]} < ${ids[i]} at index $i",
                ids[i - 1] < ids[i],
            )
        }
    }

    @Test
    fun `ids are well-formed UUIDv7 strings`() {
        val id = Uuidv7.generate()
        val pattern = Regex("^[0-9a-f]{8}-[0-9a-f]{4}-7[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")
        assertTrue("'$id' did not match UUIDv7 shape", pattern.matches(id))
    }
}
