---
description: Génère une nouvelle Activity Android à partir du mockup HTML dans C:\Users\Abdou\Desktop\screen\
argument-hint: <ActivityName> <prof|student>
---

# /newscreen — Génération d'une Activity depuis un mockup HTML

## Arguments
$ARGUMENTS

Format attendu : `<ActivityName> <role>` où :
- `<ActivityName>` : nom PascalCase **sans** le suffixe `Activity` (ex: `ProfDashboard`, `CreerClasse`, `RejoindreClasse`)
- `<role>` : `prof` ou `student` (détermine le sous-package)

Si les arguments sont absents ou mal formés, **demande à l'utilisateur** avant de générer.

## Workflow à exécuter

### 1. Lire le mockup source
- Lis **`C:\Users\Abdou\Desktop\screen\code.html`** (le HTML Stitch/Tailwind contenant la maquette)
- Lis **`C:\Users\Abdou\Desktop\screen\screen.png`** (aperçu visuel — utile pour comprendre la mise en page)

### 2. Conventions de nommage
Pour un argument `ProfDashboard` + `prof` :
- **Activity** : `app/src/main/java/com/example/attendble/ui/prof/ProfDashboardActivity.java`
- **Layout** : `app/src/main/res/layout/activity_prof_dashboard.xml` (snake_case du nom complet)
- **Package** : `com.example.attendble.ui.prof` (ou `.student`)

### 3. Conversion HTML → Android (Material 3)
Convertis le HTML/Tailwind en composants Material 3 Android natifs :

| HTML/Tailwind | Android |
|---|---|
| `<button class="bg-primary">` | `<com.google.android.material.button.MaterialButton>` |
| `<input type="text">` | `<com.google.android.material.textfield.TextInputLayout>` + `TextInputEditText` |
| `<div class="card">` | `<com.google.android.material.card.MaterialCardView>` |
| `<ul>` / `<li>` répétés | `<androidx.recyclerview.widget.RecyclerView>` + créer un item layout |
| Icônes Material Symbols | Drawable vectoriel ou `app:icon` |
| Couleurs Tailwind (`bg-primary`, etc.) | `?attr/colorPrimary`, `?attr/colorOnSurface`... (Material 3 dynamic theme) |
| Texte FR du mockup | Extraire dans `res/values/strings.xml` (pas de texte hardcodé) |

### 4. Fichiers à créer/modifier
1. **`<ActivityName>Activity.java`** dans `ui/<role>/`
   - Hérite de `AppCompatActivity`
   - `EdgeToEdge.enable()` + insets pattern (cohérent avec LoginActivity)
   - `findViewById` pour tous les composants interactifs
   - Listeners stub (TODO) pour boutons → toast `"<action> (à implémenter)"`
   - Si le screen utilise une logique métier déjà disponible (login, signup...), brancher via `ServiceLocator`

2. **`activity_<snake_case>.xml`** dans `res/layout/`
   - Root : `androidx.constraintlayout.widget.ConstraintLayout` avec `android:id="@+id/main"`
   - Material 3 components
   - Tous les `android:text` → références à `@string/...`

3. **`res/values/strings.xml`** : ajouter les nouvelles chaînes (FR)

4. **`AndroidManifest.xml`** : enregistrer l'activity
   ```xml
   <activity
       android:name=".ui.<role>.<ActivityName>Activity"
       android:exported="false" />
   ```

### 5. Règles Clean Architecture (CLAUDE.md)
- **Aucun appel direct** à Firebase, RTDB, ou InMemoryAuthRepository depuis l'Activity
- Toujours passer par `ServiceLocator.provideXxxUseCase()`
- Si un use case manque, **demander** avant d'en créer un — ne pas improviser

### 6. Commentaires
- Une ligne descriptive au-dessus de la classe (rôle de l'écran)
- Pas de commentaires sur le `what` du code (seulement le `why` si non évident)

### 7. Rapport final
À la fin, liste :
- Fichiers créés (chemins absolus)
- Strings ajoutés
- Listeners stub à implémenter ensuite
- Use cases / repositories à créer pour brancher la logique réelle

## Notes
- Le HTML dans `screen/code.html` change à chaque invocation — l'utilisateur le remplace avant d'appeler la commande
- Si `code.html` ou `screen.png` n'existent pas, signale-le
- Langue UI : **français** (selon CLAUDE.md)
- Si l'activity doit être lancée au démarrage à la place de LoginActivity, **demander** avant de modifier le manifest
