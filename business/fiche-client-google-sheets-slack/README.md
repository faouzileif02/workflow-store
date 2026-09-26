# Création auto fiche client Google Sheets + alerte Slack

> **Statut : BETA — validation/corrections en cours**

Quand un prospect remplit votre formulaire de contact, ses informations sont immédiatement ajoutées dans un Google Sheet et l'équipe commerciale reçoit une notification Slack en temps réel. Plus besoin de surveiller sa boîte mail ou de perdre les leads dans les premières minutes critiques. Un accusé de réception automatique est également envoyé au prospect.

## Pour qui ?

Équipes commerciales et marketeurs qui veulent traiter instantanément les nouveaux leads venant de leur site web.

## Prérequis

Connecter un compte Google et un compte Slack.

## Version commerciale

Le fichier JSON n8n complet reste privé. Le pack commercial est prévu pour inclure le workflow importable, les instructions de configuration et les paramètres à personnaliser.

## Validation technique

La fiche de conception indique que ce workflow doit encore être retravaillé avant commercialisation.

**Point principal à corriger :** L entete exacte des colonnes Google Sheets doit etre precisee dans le setup pour eviter les erreurs d insertion. Le statut Contacte pour un simple email automatique fausse le suivi commercial, il vaut mieux utiliser Email envoye. Le mapping du Row ID pour la mise a jour finale doit etre explicite.

## Limites

Le workflow ne déduplique pas les prospects déjà présents dans le Google Sheet, il créera une nouvelle ligne même si l'email existe déjà. Il ne s'intègre pas à un CRM tiers comme HubSpot ou Pipedrive. Le formulaire n8n natif est fonctionnel mais basique visuellement, une intégration avec Typeform ou Tally nécessiterait un noeud Webhook à la place du Form Trigger.

## Tags

formulaire contact, google sheets, notification slack, lead entrant, automation commerciale
