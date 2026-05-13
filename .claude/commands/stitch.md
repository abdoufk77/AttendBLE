---
description: Génère une prompt Stitch (en anglais) pour créer un nouvel écran AttendBLE qui suit le design system du projet
argument-hint: <description de l'écran en français ou anglais — peut aussi être vide pour utiliser le contexte de la conversation>
---

# /stitch — Génère une prompt Stitch pour un nouvel écran

## Arguments
$ARGUMENTS

## Règles de génération

### 1. Comprendre la demande
- Si `$ARGUMENTS` contient une description claire de l'écran → utilise-la directement.
- Si `$ARGUMENTS` est vide, vague, ou fait référence au contexte ("fais ça", "l'écran dont on vient de parler", "pareil que tout à l'heure", "celui-là"...) → **relis la conversation en cours** et déduis quel écran l'utilisateur veut générer. Si tu n'es pas sûr, demande une confirmation courte avant de produire la prompt.
- L'utilisateur peut écrire en français — la prompt finale envoyée à Stitch doit toujours être en **anglais**.

### 2. Sortie attendue
Tu dois produire **uniquement** un bloc de prompt prêt à coller dans Stitch. Pas d'explication avant ou après, juste le bloc dans une fence ```` ``` ````.

### 3. Contenu obligatoire de la prompt Stitch
La prompt générée doit :
- Être rédigée **en anglais**.
- Demander explicitement que **tous les textes de l'UI générée soient en anglais**.
- Préciser que l'écran doit **suivre le design system existant du projet AttendBLE** (Stitch a déjà ce contexte dans le projet, donc ne pas redéfinir les couleurs/polices, juste dire "follow the existing AttendBLE design system").
- Préciser **où s'insère l'écran** dans le flow si l'utilisateur l'a indiqué (avant/après quel écran).
- Décrire le **layout** de manière concise et structurée (top bar, contenu central, actions, états alternatifs si pertinents).
- Mentionner les **états alternatifs** (success, error, loading, empty) seulement s'ils sont pertinents pour cet écran.
- Rester **court et focalisé** — pas de re-spécification du design system, pas de longues justifications.

### 4. Structure recommandée de la prompt
```
Create a new screen "<ScreenName>" for the AttendBLE app, following the existing AttendBLE design system. All UI text must be in English.

Context: <where this screen fits in the user flow>

Layout:
- Top bar: <…>
- Main content: <…>
- Actions: <…>

[Optional] Alternate states:
- Success: <…>
- Error: <…>
```

### 5. Ce que tu ne dois PAS faire
- Ne pas générer de code Android.
- Ne pas créer de fichier.
- Ne pas ajouter d'explication autour du bloc prompt.
- Ne pas inventer de couleurs, polices ou tailles précises — le design system de Stitch s'en charge.
- Ne pas écrire la prompt en français.
