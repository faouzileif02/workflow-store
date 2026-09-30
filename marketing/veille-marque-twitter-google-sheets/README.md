# Veille marque automatique Twitter vers Google Sheets

![Veille marque automatique Twitter vers Google Sheets](./workflow-banner.svg)

> **Statut : BETA — validation/corrections en cours**

Ce workflow surveille automatiquement les mentions de votre marque sur Twitter/X et les centralise dans un tableau Google Sheets. Vous recevez un email d’alerte uniquement pour les tweets importants (plus de 5 likes ou contenant des mots négatifs). Fini de passer à côté des conversations qui vous concernent.

## Pour qui ?

Gérants de petites marques, community managers freelances et entrepreneurs qui veulent suivre leur e-réputation sur Twitter sans y passer des heures.

## Prérequis

Connexion compte Twitter/X (Bearer Token) + compte Google (Sheets & Gmail)

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Il faut corriger les limites car le plan gratuit de l API Twitter ne permet plus de chercher des tweets (plan Basic payant requis). Remplacer aussi Split In Batches par le noeud Loop et optimiser la recherche Google Sheets pour ne pas la faire en boucle.

## Limites

Le plan gratuit de l'API Twitter/X v2 limite à 500 000 tweets lus par mois et impose des délais, ce qui peut bloquer un compte avec beaucoup de mentions. Le workflow ne couvre pas Instagram, TikTok ou Facebook dont les API sont fermées ou payantes. L'analyse de sentiment est rudimentaire, basée sur une liste de mots définis à la main, pas sur un modèle IA. Les tweets supprimés entre le moment de la collecte et la consultation ne seront pas retirés de la feuille.

## Tags

veille marque, twitter monitoring, google sheets, alerte email, community management
