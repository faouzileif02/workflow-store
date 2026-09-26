# Installation — Création auto fiche client Google Sheets + alerte Slack

## 1. Préparer les comptes et services

Connecter un compte Google et un compte Slack.

## 2. Importer le workflow

Importer le fichier JSON fourni dans la version commerciale dans n8n.

## 3. Configurer les credentials

Créer les credentials nécessaires directement dans n8n. Ne jamais mettre de clé API ou mot de passe en dur dans le workflow.

## 4. Adapter les identifiants et destinations

Remplacer les identifiants de démonstration, IDs de fichiers, adresses email, bases Notion, feuilles Google Sheets ou autres valeurs propres au client.

## 5. Tester avant activation

Exécuter le workflow sur des données de test et vérifier chaque branche avant de l'activer en production.

## Point à corriger avant vente

L entete exacte des colonnes Google Sheets doit etre precisee dans le setup pour eviter les erreurs d insertion. Le statut Contacte pour un simple email automatique fausse le suivi commercial, il vaut mieux utiliser Email envoye. Le mapping du Row ID pour la mise a jour finale doit etre explicite.

## Limites connues

Le workflow ne déduplique pas les prospects déjà présents dans le Google Sheet, il créera une nouvelle ligne même si l'email existe déjà. Il ne s'intègre pas à un CRM tiers comme HubSpot ou Pipedrive. Le formulaire n8n natif est fonctionnel mais basique visuellement, une intégration avec Typeform ou Tally nécessiterait un noeud Webhook à la place du Form Trigger.

