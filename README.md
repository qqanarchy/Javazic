# Javazic

Application Java de gestion et lecture de musique, avec :

- un catalogue demo local
- des recherches et tendances Jamendo
- des recherches et tendances Apple/iTunes
- des playlists multi-source
- une interface console et une interface JavaFX

## Prerequis

- Java 17+ pour le projet
- JavaFX pour l'interface graphique

## Lancer la version graphique

Exemple avec JavaFX :

```powershell
java --module-path "C:\chemin\vers\javafx-sdk\lib" --add-modules javafx.controls,javafx.fxml,javafx.media -cp out com.javazic.MainGUI
```

## Structure

- `src/` : code source
- `META-INF/` : manifest
- `build.sh` et `run.sh` : scripts utilitaires
