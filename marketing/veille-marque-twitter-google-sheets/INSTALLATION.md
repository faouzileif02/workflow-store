# Installation — Veille marque automatique Twitter vers Google Sheets

## Prérequis

Connexion compte Twitter/X (Bearer Token) + compte Google (Sheets & Gmail)

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les IDs, feuilles, comptes et paramètres de démonstration.
4. Tester avec des données de test.
5. Valider avant production : Il faut corriger les limites car le plan gratuit de l API Twitter ne permet plus de chercher des tweets (plan Basic payant requis). Remplacer aussi Split In Batches par le noeud Loop et optimiser la recherche Google Sheets pour ne pas la faire en boucle..

## Limites connues

Le plan gratuit de l'API Twitter/X v2 limite à 500 000 tweets lus par mois et impose des délais, ce qui peut bloquer un compte avec beaucoup de mentions. Le workflow ne couvre pas Instagram, TikTok ou Facebook dont les API sont fermées ou payantes. L'analyse de sentiment est rudimentaire, basée sur une liste de mots définis à la main, pas sur un modèle IA. Les tweets supprimés entre le moment de la collecte et la consultation ne seront pas retirés de la feuille.

