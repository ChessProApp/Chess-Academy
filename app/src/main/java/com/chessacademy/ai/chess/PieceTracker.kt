package com.chessacademy.ai.chess

import kotlin.math.abs
import kotlin.math.max

data class TrackedPiece(val id: Int, val piece: Pc, val square: Int)

/**
 * Verfolgt Figuren-Identitäten über Stellungswechsel hinweg, damit die UI jede Figur
 * vom Start- zum Zielfeld gleiten lassen kann (statt zu verschwinden und neu zu erscheinen).
 *
 * Funktioniert allgemein per Stellungsvergleich – für normale Züge, Schlagen, Rochade (Turm gleitet mit),
 * En passant, Umwandlung (Bauer behält die ID) sowie für Zugrücknahme und Vor/Zurück im Zugverlauf.
 */
class PieceTracker {
    private var nextId = 1
    var pieces: List<TrackedPiece> = emptyList()
        private set

    /** Neustart ohne Animation (neue IDs). */
    fun reset(pos: Position) {
        pieces = buildList {
            for (sq in 0..63) pos.squares[sq]?.let { add(TrackedPiece(nextId++, it, sq)) }
        }
    }

    /**
     * Überführt den Stand in [pos]. [hintFrom]/[hintTo] (optional) kennzeichnen den letzten Zug
     * und machen Umwandlungen eindeutig. Rückgabe: Figuren, die vom Brett verschwunden sind
     * (mit ihrer letzten Position) – z. B. für die Schlag-Animation.
     */
    fun update(pos: Position, hintFrom: Int = -1, hintTo: Int = -1): List<TrackedPiece> {
        val old = pieces
        val oldMatched = BooleanArray(old.size)
        val newMatched = BooleanArray(64)
        val result = HashMap<Int, TrackedPiece>() // neues Feld -> Figur

        // 1) unveränderte Figuren behalten ihre ID
        for ((i, o) in old.withIndex()) {
            if (pos.squares[o.square] == o.piece && !newMatched[o.square]) {
                result[o.square] = o
                oldMatched[i] = true
                newMatched[o.square] = true
            }
        }

        // 2) Zug-Hinweis: Figur von hintFrom landet auf hintTo (auch bei Umwandlung)
        if (hintFrom in 0..63 && hintTo in 0..63 && !newMatched[hintTo] && pos.squares[hintTo] != null) {
            val i = old.indexOfFirst { it.square == hintFrom }
            if (i >= 0 && !oldMatched[i]) {
                result[hintTo] = TrackedPiece(old[i].id, pos.squares[hintTo]!!, hintTo)
                oldMatched[i] = true
                newMatched[hintTo] = true
            }
        }

        // 3) Rest: gleiche Figur, kleinster Abstand (greedy, deterministisch)
        data class Cand(val oi: Int, val sq: Int, val dist: Int)
        val cands = ArrayList<Cand>()
        for ((i, o) in old.withIndex()) {
            if (oldMatched[i]) continue
            for (sq in 0..63) {
                if (newMatched[sq] || pos.squares[sq] != o.piece) continue
                cands.add(Cand(i, sq, dist(o.square, sq)))
            }
        }
        cands.sortWith(compareBy<Cand> { it.dist }.thenBy { it.oi }.thenBy { it.sq })
        for (c in cands) {
            if (oldMatched[c.oi] || newMatched[c.sq]) continue
            result[c.sq] = TrackedPiece(old[c.oi].id, old[c.oi].piece, c.sq)
            oldMatched[c.oi] = true
            newMatched[c.sq] = true
        }

        // 4) neue Figuren (z. B. Stellungsaufbau)
        for (sq in 0..63) {
            val p = pos.squares[sq] ?: continue
            if (!newMatched[sq]) result[sq] = TrackedPiece(nextId++, p, sq)
        }

        val removed = old.filterIndexed { i, _ -> !oldMatched[i] }
        pieces = result.values.sortedBy { it.id }
        return removed
    }

    private fun dist(a: Int, b: Int): Int = max(abs(Sq.file(a) - Sq.file(b)), abs(Sq.rank(a) - Sq.rank(b)))
}
