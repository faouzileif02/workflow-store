# Tri automatique des CV par email vers Notion

> **Statut : BETA — 3 variantes regroupées**

Les RH de petites structures perdent un temps fou à copier manuellement les CV reçus par email dans leur tableau de suivi. Ce workflow détecte automatiquement les nouvelles candidatures, extrait les informations clés du PDF et crée une fiche structurée dans Notion. Un email de confirmation est envoyé au candidat et une notification arrive sur Slack.

## Pour qui ?

Responsables RH et recruteurs de PME qui reçoivent les candidatures par email.

## Prérequis

Connectez vos comptes Gmail, Notion, Slack et OpenAI.

## Versions disponibles

Trois variantes de conception existent dans le dossier source. Elles sont regroupées ici comme un seul produit pour éviter les doublons dans le catalogue.

## Points à finaliser avant commercialisation

- Variante 1 (10 nœuds) : Le setup doit imperativement lister les colonnes a creer dans la base Notion pour que le mapping fonctionne. Il manque aussi une etape pour uploader le fichier PDF original dans Notion ou Google Drive.
- Variante 2 (12 nœuds) : Un filtre pour isoler uniquement les pieces jointes PDF et ignorer les images de signature. Il faut aussi imperativement uploader le fichier original dans Notion, aucun recruteur ne se contentera du texte brut.
- Variante 3 (9 nœuds) : Le setup doit imperativement detailler les colonnes a creer dans la base Notion. Il faut aussi ajouter une condition juste apres Gmail pour verifier la presence de la piece jointe avant de lancer l extraction.

## Limites

Le workflow ne lit pas les CV au format Word ou image scannee, seulement les PDF avec texte selectionnable. Il ne note pas les candidats ni ne les compare entre eux. Il ne gere pas les candidatures soumises via un formulaire web externe. Si le PDF est mal structure, l extraction peut rater certains champs.

## Tags

recrutement, cv, notion, automatisation, rh, email, parsing
