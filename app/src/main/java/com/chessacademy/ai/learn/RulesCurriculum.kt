package com.chessacademy.ai.learn

/** Gesamter Regel-Lehrplan: Anfänger → Experte, aufbauend (Reihenfolge = order). */
object RulesCurriculum {
    val all: List<RuleLesson> by lazy { (RulesBeginner.lessons + RulesAdvanced.lessons).sortedBy { it.order } }
    fun byId(id: String): RuleLesson? = all.firstOrNull { it.id == id }
    fun byLevel(level: RuleLevel): List<RuleLesson> = all.filter { it.level == level }
}
