package com.chessacademy.ai.tests

fun main(args: Array<String>) {
    println("Chess Academy – JVM-Tests der Logikschicht")
    RulesTests.run()
    EngineTests.run()
    ContentTests.run()
    TrainingTests.run()
    kotlin.system.exitProcess(T.finish())
}
