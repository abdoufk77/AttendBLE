---
description: Crée un commit git avec un message simple sur une seule ligne
allowed-tools: Bash(git add:*), Bash(git status:*), Bash(git diff:*), Bash(git log:*), Bash(git commit:*)
---

Fais un commit git en suivant ces règles strictes :

1. Lance `git status` et `git diff` pour voir les changements.
2. Stage les fichiers pertinents (évite les secrets : `local.properties`, `*.jks`, `google-services.json`, `.env`).
3. Rédige un message de commit **très simple, sur UNE SEULE LIGNE**, en anglais, qui décrit l'essentiel du changement.
4. Crée le commit avec `git commit -m "<message>"`.
5. **NE PAS** inclure :
   - de mention de Claude, Anthropic, ou `Co-Authored-By`
   - de heredoc, de lignes multiples, ou de corps de message
   - d'emoji sauf si demandé explicitement
6. Termine par `git status` pour confirmer.

Si l'utilisateur a passé du texte en argument, utilise-le comme message tel quel : $ARGUMENTS
