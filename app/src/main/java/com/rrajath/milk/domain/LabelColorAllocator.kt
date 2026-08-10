package com.rrajath.milk.domain

// The prototype ships a 6-entry palette (PRD's "~10" was superseded by the
// prototype per the visual-fidelity decision). Allocation rule (TDD §4.2):
// lowest index not currently held by a live label; once exhausted, cycle by
// count(live labels) mod palette size, so two labels created in one paste
// never collide on color even before the second insert commits.
object LabelColorAllocator {
    const val PALETTE_SIZE = 6

    fun nextColorIndex(liveColorIndices: List<Int>): Int {
        val used = liveColorIndices.toHashSet()
        for (i in 0 until PALETTE_SIZE) {
            if (i !in used) return i
        }
        return liveColorIndices.size % PALETTE_SIZE
    }
}
