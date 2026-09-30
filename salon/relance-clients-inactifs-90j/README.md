# Relance SMS automatique clients inactifs 90 jours

![Relance SMS automatique clients inactifs 90 jours](./workflow-banner.svg)

> **Statut : BETA — workflow salon générique, séparé de SalonPilot**

Votre salon perd régulièrement des clients fidèles qui ne reviennent plus sans que vous le remarquiez. Ce workflow identifie automatiquement les clients qui n’ont pas pris rendez-vous depuis plus de 90 jours et leur envoie un SMS de relance personnalisé. Vous recevez chaque lundi un récapitulatif par email avec le nombre de personnes relancées.

## Pour qui ?

Gérants de salons de coiffure qui veulent récupérer leurs clients inactifs sans perdre de temps.

## Prérequis

Connecter un compte Google et un compte Twilio.

## Version commerciale

Le JSON n8n complet reste privé. Ce workflow est présenté comme automatisation indépendante pour salons et n'est pas intégré à SalonPilot dans ce dépôt.

## Point à corriger avant commercialisation

Il faut fournir un lien vers un modele Google Sheets preconfigure avec les bons onglets et colonnes. Il manque aussi un noeud Loop pour que la pause de deux secondes s applique bien a chaque SMS individuel.

## Limites

Le workflow ne sait pas si le client a répondu au SMS ou pris rendez-vous après la relance, ce suivi doit être fait manuellement dans le Google Sheet. Il ne gère pas les désabonnements SMS, le gérant doit tenir à jour une liste noire manuellement. Il ne se connecte à aucun logiciel de caisse ou de réservation, les données clients doivent être exportées et collées dans le Google Sheet.

## Tags

relance clients, sms automatique, salon coiffure, fidélisation, twilio
