# Installation — Relance SMS automatique clients inactifs 90 jours

## Prérequis

Connecter un compte Google et un compte Twilio.

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les numéros, IDs, emails et paramètres de démonstration.
4. Tester avec des données et numéros de test.
5. Valider avant production : Il faut fournir un lien vers un modele Google Sheets preconfigure avec les bons onglets et colonnes. Il manque aussi un noeud Loop pour que la pause de deux secondes s applique bien a chaque SMS individuel..

## Limites connues

Le workflow ne sait pas si le client a répondu au SMS ou pris rendez-vous après la relance, ce suivi doit être fait manuellement dans le Google Sheet. Il ne gère pas les désabonnements SMS, le gérant doit tenir à jour une liste noire manuellement. Il ne se connecte à aucun logiciel de caisse ou de réservation, les données clients doivent être exportées et collées dans le Google Sheet.

