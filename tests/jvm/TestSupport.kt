package com.chessacademy.ai.tests

/** Minimaler Test-Runner ohne externe Abhängigkeiten (läuft mit kotlinc + java). */
object T {
    var passed = 0
    var failed = 0
    val failures = ArrayList<String>()
    private var current = ""

    fun suite(name: String, body: () -> Unit) {
        current = name
        val before = failed
        try { body() } catch (e: Throwable) {
            failed++
            failures.add("[$name] Ausnahme: $e\n" + e.stackTrace.take(6).joinToString("\n") { "    at $it" })
        }
        println("  ${if (failed == before) "OK  " else "FAIL"} $name")
    }

    fun check(cond: Boolean, msg: String) {
        if (cond) passed++ else { failed++; failures.add("[$current] $msg") }
    }

    fun <A> eq(actual: A, expected: A, msg: String) {
        if (actual == expected) passed++ else { failed++; failures.add("[$current] $msg: erwartet <$expected>, erhalten <$actual>") }
    }

    fun finish(): Int {
        println()
        println("Ergebnis: $passed Prüfungen bestanden, $failed fehlgeschlagen")
        failures.forEach { println("  ✗ $it") }
        return if (failed == 0) 0 else 1
    }
}
