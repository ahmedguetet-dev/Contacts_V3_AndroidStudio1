# TP Android - Gestion des contacts V3

Projet Kotlin / interfaces XML : une activité (`MainActivity`) + 3 fragments dynamiques
(`ListeContactsFragment`, `DetailContactFragment`, `CarteContactFragment`), RecyclerView filtrable
et affichage Google Maps de la position GPS des contacts.

## Ouvrir et lancer
1. Android Studio > **File > Open** > sélectionner le dossier `contactslistview-master`.
2. Attendre la fin du **Gradle Sync** (JDK 17 : *Settings > Build Tools > Gradle > Gradle JDK*).
3. Créer/démarrer un émulateur **avec Google Play / Google APIs** (Device Manager) et attendre
   l'écran d'accueil complet, ou brancher un téléphone (débogage USB).
4. Choisir la configuration **app** dans la barre d'outils, puis **Run** (Shift+F10).

## Google Maps (clé non incluse dans le dépôt)
1. Google Cloud Console : créer un projet, activer **Maps SDK for Android**, créer une clé API.
2. La restreindre à Android : package `com.example.contacts` + empreinte SHA-1 de debug
   (`./gradlew signingReport`) et à *Maps SDK for Android*.
3. Ouvrir (ou créer) le fichier **`local.properties`** à la racine et ajouter :
   ```
   MAPS_API_KEY=VOTRE_CLE
   ```
   (modèle : `local.properties.example`). Ce fichier est dans `.gitignore` : la clé n'est jamais publiée.
   Alternatives : `~/.gradle/gradle.properties` ou la variable d'environnement `MAPS_API_KEY`.
4. Relancer l'application (rebuild).

Sans clé, l'application fonctionne normalement ; l'écran Carte affiche simplement un message
explicite à la place de la carte.

## Architecture
- `MainActivity` : héberge les fragments (`FragmentContainerView`), `replace()` + `addToBackStack()`,
  bouton Retour/Up, aucun fragment en double après rotation (création initiale seulement si `savedInstanceState == null`).
- `ContactsViewModel` (portée Activity) : liste en mémoire, `LiveData` observée par la liste -> mise à jour immédiate après enregistrement.
  Choix justifié : la liste et le détail partagent le même état et il survit à la rotation.
- Les fragments échangent l'**identifiant** du contact par `Bundle` (jamais la position de ligne).
- `ContactFilter` : logique de recherche pure (nom, prénom, téléphone, insensible à la casse et aux accents).
- `DetailEditViewModel` (portée Fragment) : photo choisie mais non enregistrée (survit à la rotation / à la carte).

## Fonctionnalités
- Liste RecyclerView (photo, prénom + nom, téléphone), recherche en direct, message si aucun résultat
- Détail : modification, validation (nom/prénom/téléphone obligatoires, e-mail et coordonnées valides)
- Enregistrer -> retour à la liste actualisée ; Annuler -> aucune modification conservée
- Appeler (`ACTION_DIAL`), E-mail (`ACTION_SENDTO` mailto), Galerie, Caméra (aperçu) avec gestion des annulations/applications absentes
- Coordonnées GPS facultatives (latitude -90..90, longitude -180..180), bouton « Voir sur la carte »
- Carte : centrée sur le contact, marqueur portant son nom, messages si coordonnées absentes/invalides

Les données sont gardées en mémoire (stockage permanent non demandé par l'énoncé).

## Tests
- Unitaires (filtre) : `./gradlew testDebugUnitTest`
- Manuels : voir la section 9 de l'énoncé.
