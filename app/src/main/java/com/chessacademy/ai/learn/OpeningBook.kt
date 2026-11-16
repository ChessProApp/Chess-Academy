package com.chessacademy.ai.learn

/** Eröffnungsbibliothek und -erkennung. Alle SAN-Folgen sind testgeprüft (legal von der Grundstellung). */
object OpeningBook {
    val entries: List<OpeningEntry> = listOf(
        OpeningEntry("B00", "Königsbauernspiel", "e4", "Das Zentrum sofort besetzen und Läufer sowie Dame öffnen."),
        OpeningEntry("C20", "Offenes Spiel", "e4 e5", "Beide Seiten kämpfen um das Zentrum."),
        OpeningEntry("C23", "Läuferspiel", "e4 e5 Bc4", "Früher Druck auf f7."),
        OpeningEntry("C50", "Italienische Partie", "e4 e5 Nf3 Nc6 Bc4", "Schnelle Entwicklung und Druck auf f7."),
        OpeningEntry("C53", "Giuoco Piano", "e4 e5 Nf3 Nc6 Bc4 Bc5", "Ruhiger Aufbau mit c3 und d4."),
        OpeningEntry("C51", "Evans-Gambit", "e4 e5 Nf3 Nc6 Bc4 Bc5 b4", "Bauernopfer für schnelle Zentrumsöffnung."),
        OpeningEntry("C55", "Zweispringerspiel im Nachzug", "e4 e5 Nf3 Nc6 Bc4 Nf6", "Schwarz kontert direkt im Zentrum."),
        OpeningEntry("C60", "Spanische Partie", "e4 e5 Nf3 Nc6 Bb5", "Druck auf den Verteidiger des Zentrums."),
        OpeningEntry("C68", "Spanisch, Abtauschvariante", "e4 e5 Nf3 Nc6 Bb5 a6 Bxc6", "Strukturelle Vorteile im Endspiel."),
        OpeningEntry("C78", "Spanisch, Hauptvariante", "e4 e5 Nf3 Nc6 Bb5 a6 Ba4 Nf6 O-O", "Klassische Entwicklung mit schneller Rochade."),
        OpeningEntry("C60", "Berliner Verteidigung", "e4 e5 Nf3 Nc6 Bb5 Nf6", "Solide, oft mit Damentausch."),
        OpeningEntry("C45", "Schottische Partie", "e4 e5 Nf3 Nc6 d4 exd4 Nxd4", "Zentrum öffnen und Initiative gewinnen."),
        OpeningEntry("C30", "Königsgambit", "e4 e5 f4", "Scharfes Bauernopfer um das Zentrum."),
        OpeningEntry("C42", "Russische Verteidigung (Petrow)", "e4 e5 Nf3 Nf6", "Symmetrisches Gegenspiel."),
        OpeningEntry("C41", "Philidor-Verteidigung", "e4 e5 Nf3 d6", "Solide, aber passiv."),
        OpeningEntry("C44", "Wiener Partie", "e4 e5 Nc3", "Flexibler Aufbau mit f4 oder Nf3."),
        OpeningEntry("B20", "Sizilianische Verteidigung", "e4 c5", "Ungleiches Spiel: Schwarz kämpft um das Zentrum von der Flanke."),
        OpeningEntry("B27", "Sizilianisch, Offene Variante", "e4 c5 Nf3 d6 d4 cxd4 Nxd4", "Weiß öffnet das Zentrum."),
        OpeningEntry("B90", "Sizilianisch, Najdorf", "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 a6", "Flexible Hauptvariante mit a6."),
        OpeningEntry("B70", "Sizilianisch, Drachenvariante", "e4 c5 Nf3 d6 d4 cxd4 Nxd4 Nf6 Nc3 g6", "Fianchetto des schwarzen Läufers."),
        OpeningEntry("B33", "Sizilianisch, Sveshnikov", "e4 c5 Nf3 Nc6 d4 cxd4 Nxd4 Nf6 Nc3 e5", "Schwarz bekommt dynamisches Spiel um d5."),
        OpeningEntry("B22", "Sizilianisch, Alapin", "e4 c5 c3", "Weiß baut ein starkes Bauernzentrum auf."),
        OpeningEntry("B23", "Sizilianisch, Geschlossen", "e4 c5 Nc3", "Langsamer, positioneller Aufbau."),
        OpeningEntry("C00", "Französische Verteidigung", "e4 e6", "Solide Bauernkette mit Konter auf d4."),
        OpeningEntry("C03", "Französisch, Tarrasch", "e4 e6 d4 d5 Nd2", "Vermeidet die Fesselung Lb4."),
        OpeningEntry("C11", "Französisch, Klassisch", "e4 e6 d4 d5 Nc3 Nf6", "Spannung im Zentrum."),
        OpeningEntry("C15", "Französisch, Winawer", "e4 e6 d4 d5 Nc3 Bb4", "Scharfe Fesselung auf c3."),
        OpeningEntry("C01", "Französisch, Abtauschvariante", "e4 e6 d4 d5 exd5 exd5", "Symmetrisches, ruhiges Spiel."),
        OpeningEntry("C02", "Französisch, Vorstoßvariante", "e4 e6 d4 d5 e5", "Raumvorteil für Weiß."),
        OpeningEntry("B10", "Caro-Kann-Verteidigung", "e4 c6", "Solide Verteidigung mit d5."),
        OpeningEntry("B12", "Caro-Kann, Vorstoßvariante", "e4 c6 d4 d5 e5", "Weiß gewinnt Raum."),
        OpeningEntry("B18", "Caro-Kann, Klassisch", "e4 c6 d4 d5 Nc3 dxe4 Nxe4 Bf5", "Läufer vor der Bauernkette entwickeln."),
        OpeningEntry("B13", "Caro-Kann, Abtausch", "e4 c6 d4 d5 exd5 cxd5", "Symmetrische Struktur."),
        OpeningEntry("B01", "Skandinavische Verteidigung", "e4 d5", "Sofortiger Angriff auf das Zentrum."),
        OpeningEntry("B02", "Aljechin-Verteidigung", "e4 Nf6", "Lockt Weiß mit Bauernvorstößen nach vorn."),
        OpeningEntry("B07", "Pirc-Verteidigung", "e4 d6 d4 Nf6 Nc3 g6", "Hypermoderner Aufbau."),
        OpeningEntry("B06", "Moderne Verteidigung", "e4 g6", "Flexibler Aufbau mit Fianchetto."),
        OpeningEntry("A40", "Damenbauernspiel", "d4", "Das Zentrum besetzen und Läufer c1 öffnen."),
        OpeningEntry("D00", "Geschlossenes Spiel", "d4 d5", "Symmetrische Antwort."),
        OpeningEntry("D06", "Damengambit", "d4 d5 c4", "Druck auf d5."),
        OpeningEntry("D20", "Damengambit, angenommen", "d4 d5 c4 dxc4", "Schwarz nimmt den Bauern."),
        OpeningEntry("D30", "Damengambit, abgelehnt", "d4 d5 c4 e6", "Solide Verteidigung des Zentrums."),
        OpeningEntry("D35", "Damengambit, Orthodoxe Verteidigung", "d4 d5 c4 e6 Nc3 Nf6 Bg5", "Klassische Hauptlinie."),
        OpeningEntry("D10", "Slawische Verteidigung", "d4 d5 c4 c6", "Zentrum halten und Läufer c8 freihalten."),
        OpeningEntry("D43", "Halbslawisch", "d4 d5 c4 c6 Nf3 Nf6 Nc3 e6", "Flexibler Aufbau mit Springerzug."),
        OpeningEntry("D32", "Tarrasch-Verteidigung", "d4 d5 c4 e6 Nc3 c5", "Aktives Gegenspiel."),
        OpeningEntry("E00", "Katalanisch", "d4 Nf6 c4 e6 g3", "Läufer auf der langen Diagonale."),
        OpeningEntry("E20", "Nimzo-Indisch", "d4 Nf6 c4 e6 Nc3 Bb4", "Fesselung des Springers c3."),
        OpeningEntry("E12", "Damenindisch", "d4 Nf6 c4 e6 Nf3 b6", "Fianchetto des Damenläufers."),
        OpeningEntry("E60", "Königsindisch", "d4 Nf6 c4 g6", "Schwarz greift das Zentrum später an."),
        OpeningEntry("E70", "Königsindisch, Klassisch", "d4 Nf6 c4 g6 Nc3 Bg7 e4 d6", "Weiß baut großes Zentrum, Schwarz kontert."),
        OpeningEntry("D80", "Grünfeld-Indisch", "d4 Nf6 c4 g6 Nc3 d5", "Schwarz greift das Zentrum direkt an."),
        OpeningEntry("A60", "Benoni", "d4 Nf6 c4 c5 d5", "Asymmetrische Bauernstruktur."),
        OpeningEntry("A80", "Holländische Verteidigung", "d4 f5", "Kampf um e4."),
        OpeningEntry("D02", "Londoner System", "d4 d5 Nf3 Nf6 Bf4", "Solider Aufbau mit Lf4."),
        OpeningEntry("A45", "Trompowsky", "d4 Nf6 Bg5", "Früher Läuferausfall."),
        OpeningEntry("D05", "Colle-System", "d4 d5 Nf3 Nf6 e3", "Ruhiger Aufbau mit c3 und Nbd2."),
        OpeningEntry("A10", "Englische Eröffnung", "c4", "Flankeneröffnung mit Zentrumskontrolle."),
        OpeningEntry("A20", "Englisch, Symmetrisch", "c4 c5", "Spiegelsymmetrie."),
        OpeningEntry("A04", "Réti-Eröffnung", "Nf3", "Flexibel: Zentrum später besetzen."),
        OpeningEntry("A09", "Réti, Hauptlinie", "Nf3 d5 c4", "Zentrum angreifen."),
        OpeningEntry("A02", "Bird-Eröffnung", "f4", "Kontrolle von e5."),
        OpeningEntry("A01", "Larsen-Eröffnung", "b3", "Fianchetto des Damenläufers."),
        OpeningEntry("A00", "Grob-Eröffnung", "g4", "Ungewöhnlicher Flankenangriff."),
        OpeningEntry("A00", "Sokolski-Eröffnung", "b4", "Raumgewinn am Damenflügel.")
    )

    private fun norm(s: String) = s.trimEnd('+', '#', '!', '?')

    /** Längster passender Eintrag, dessen Zugfolge ein Präfix der gespielten Züge (SAN) ist. */
    fun detect(playedSan: List<String>): OpeningEntry? {
        if (playedSan.isEmpty()) return null
        val played = playedSan.map(::norm)
        var best: OpeningEntry? = null
        var bestLen = 0
        for (e in entries) {
            val m = e.moves.split(" ")
            if (m.size <= played.size && m.size > bestLen && m.indices.all { m[it] == played[it] }) { best = e; bestLen = m.size }
        }
        return best
    }

    /** Einträge, deren Zugfolge mit den bisher gespielten Zügen beginnt (zum Weiterblättern). */
    fun continuations(playedSan: List<String>): List<OpeningEntry> =
        entries.filter { e -> val m = e.moves.split(" "); m.size > playedSan.size && playedSan.indices.all { m[it] == norm(playedSan[it]) } }
}
