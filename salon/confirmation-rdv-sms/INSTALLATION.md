# Installation — Confirmation RDV automatique par SMS pour salon de coiffure

## Prérequis

Compte Twilio (numéro SMS actif) + Compte Google (Sheets et Gmail)

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les numéros, IDs, emails et paramètres de démonstration.
4. Tester avec des données et numéros de test.
5. Valider avant production : L architecture est fausse, il faut separer l envoi et la reception en deux workflows distincts. Placer un noeud Trigger au milieu d un flux apres un noeud Wait est impossible dans n8n. Il manque aussi la logique de recherche pour lier le numero du SMS entrant a la bonne ligne du Google Sheet..

## Limites connues

Le workflow ne rebooке pas automatiquement le creneau libere avec un client en liste d'attente, il se contente d'alerter le gerant par email. Il ne gere pas les annulations faites moins d'une heure avant le rendez-vous. Si le salon utilise un logiciel de caisse avec sa propre base de donnees, il faudra exporter les rendez-vous manuellement vers Google Sheets.

