# Confirmation RDV automatique par SMS pour salon de coiffure

![Confirmation RDV automatique par SMS pour salon de coiffure](./workflow-banner.svg)

> **Statut : BETA — workflow salon générique, séparé de SalonPilot**

Les salons de coiffure perdent 10 à 20 % de leurs rendez-vous à cause des no-shows. Ce workflow envoie automatiquement un SMS de confirmation la veille et met à jour le planning selon la réponse du client. Le gérant est alerté par email en cas d'annulation pour pouvoir réattribuer le créneau rapidement.

## Pour qui ?

Gérants et propriétaires de salons de coiffure qui utilisent Google Sheets pour gérer leurs rendez-vous.

## Prérequis

Compte Twilio (numéro SMS actif) + Compte Google (Sheets et Gmail)

## Version commerciale

Le JSON n8n complet reste privé. Ce workflow est présenté comme automatisation indépendante pour salons et n'est pas intégré à SalonPilot dans ce dépôt.

## Point à corriger avant commercialisation

L architecture est fausse, il faut separer l envoi et la reception en deux workflows distincts. Placer un noeud Trigger au milieu d un flux apres un noeud Wait est impossible dans n8n. Il manque aussi la logique de recherche pour lier le numero du SMS entrant a la bonne ligne du Google Sheet.

## Limites

Le workflow ne rebooке pas automatiquement le creneau libere avec un client en liste d'attente, il se contente d'alerter le gerant par email. Il ne gere pas les annulations faites moins d'une heure avant le rendez-vous. Si le salon utilise un logiciel de caisse avec sa propre base de donnees, il faudra exporter les rendez-vous manuellement vers Google Sheets.

## Tags

salon coiffure, confirmation rdv, sms automatique, no-show, relance client
