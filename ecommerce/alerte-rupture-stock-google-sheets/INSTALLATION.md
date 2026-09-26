# Installation

## 1. Préparer Google Sheets

Créer une feuille avec les colonnes suivantes :

- `produit`
- `quantité actuelle`
- `seuil d'alerte`
- `Dernière alerte envoyée`

## 2. Connecter Google à n8n

Configurer :
- Google Sheets OAuth2
- Gmail OAuth2

## 3. Configurer le workflow

Dans la version JSON complète :
- remplacer `REPLACE_WITH_SPREADSHEET_ID` par l'ID du Google Sheet ;
- sélectionner le bon onglet ;
- remplacer `gerant@example.com` par l'adresse email du responsable.

## 4. Déclenchement

Le workflow est prévu pour s'exécuter tous les jours à **07:00**.

## 5. Test

Avant production :
1. dupliquer la feuille ;
2. mettre un produit sous son seuil ;
3. exécuter le workflow manuellement ;
4. vérifier l'email reçu ;
5. vérifier la colonne `Dernière alerte envoyée`.
