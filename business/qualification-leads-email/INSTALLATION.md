# Installation — Qualification automatique des leads par email

## Prérequis

Connecter un compte Gmail (IMAP + SMTP), une clé API OpenAI et un compte Google Sheets.

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials dans n8n.
3. Remplacer les IDs, feuilles, emails et paramètres de démonstration.
4. Exécuter des tests sur des données de test.
5. Corriger/valider le point suivant avant production : Le JSON est corrompu car il manque le noeud declencheur Email Trigger mentionne dans la description. Il faut imperativement reexporter le workflow depuis n8n en s assurant de selectionner tous les noeuds. Sans cela, le template est techniquement invalide..

## Limites connues

Le workflow ne lit pas les pièces jointes ni les emails en image. Il ne met pas à jour un vrai CRM comme HubSpot ou Salesforce. La classification dépend de la qualité de rédaction du prospect et peut se tromper sur des emails courts ou ambigus. Il ne gère pas les réponses aux relances, seulement les premiers contacts.

