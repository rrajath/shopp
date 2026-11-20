package com.rrajath.shopp.domain

// Keeps the stepper and the " xN" suffix to two digits, so neither can push
// the title out of its row.
const val MIN_QUANTITY = 1
const val MAX_QUANTITY = 99

fun clampQuantity(quantity: Int): Int = quantity.coerceIn(MIN_QUANTITY, MAX_QUANTITY)

// A quantity of 1 is the common case, so it stays implicit: "Milk", not
// "Milk x1". Used by every surface that shows an item title.
fun titleWithQuantity(title: String, quantity: Int): String =
    if (quantity > 1) "$title x$quantity" else title

// Placeholder shown in Quick Add after a submit dropped duplicates. Each
// pair is (title, quantity already on the list). Names the first one and
// counts the rest, so a long paste can't overflow the input line.
fun duplicateHint(duplicates: List<Pair<String, Int>>): String? {
    val (title, quantity) = duplicates.firstOrNull() ?: return null
    val rest = duplicates.size - 1
    val more = if (rest > 0) " and $rest more" else ""
    return "$title x$quantity$more already added"
}
