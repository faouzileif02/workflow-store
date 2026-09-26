# Facebook Posts + Reels — n8n

> **Statut : TEMPLATE NETTOYÉ**

Workflow n8n avec deux branches indépendantes dans le même projet :

- **Post Facebook normal** : image + texte
- **Reel Facebook** : vidéo + texte

## Branche Reel

```text
Déclencheur Reel
→ sélection vidéo
→ génération du texte
→ publication Reel Facebook
→ mémorisation de la vidéo utilisée
```

## Branche Post Facebook normal

```text
Déclencheur Post
→ sélection image
→ génération du texte
→ publication Facebook image + texte
→ mémorisation de l'image utilisée
```

## Template nettoyé

Le JSON fourni ne contient pas de credentials enregistrés, clés API, tokens, ID de page Facebook personnel, IDs Data Table de l'instance d'origine ni instanceId n8n.

Les nœuds et la logique des deux branches sont conservés.

## Fichier n8n

`Facebook-Posts-et-Reels-Template-Clean.json`

## Catégorie

Marketing / Facebook / Réseaux sociaux / Reels
