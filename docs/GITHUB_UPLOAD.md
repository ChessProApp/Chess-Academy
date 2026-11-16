# Auf GitHub hochladen (neues Repository)

1. Auf github.com einloggen → **New repository** → Name z. B. `chess-academy`, **leer** anlegen (ohne README/.gitignore/Lizenz).
2. ZIP entpacken. Wichtig: Es muss der **Inhalt** des Ordners `Chess-Academy` hochgeladen werden, damit `app/`, `.github/`, `build.gradle.kts` usw. direkt im Wurzelverzeichnis liegen.
3. Variante ohne Git: Im leeren Repository **uploading an existing file** wählen und den gesamten Ordnerinhalt per Drag & Drop hineinziehen (auch den versteckten Ordner `.github`; falls der Browser ihn nicht mitnimmt: im Repository **Add file → Create new file**, Name `.github/workflows/build_apk.yml` eingeben und den Inhalt der Datei einfügen). Mit **Commit changes** auf Branch `main` bestätigen.
4. Variante mit Git:
   ```
   cd Chess-Academy
   git init -b main
   git add .
   git commit -m "Chess Academy"
   git remote add origin https://github.com/<DEIN-NAME>/chess-academy.git
   git push -u origin main
   ```
5. Reiter **Actions**: Der Lauf „Build Release APK“ startet automatisch (Actions ggf. einmal aktivieren). Fertig → **Artifacts** → `Chess-Academy-AI-APK`.
6. Schlägt der Lauf fehl: Auf den roten Lauf klicken, den fehlgeschlagenen Schritt öffnen und den Fehlertext kopieren.
