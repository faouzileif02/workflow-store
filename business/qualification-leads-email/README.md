# Qualification automatique des leads par email

## 🛒 Acheter ce workflow

[**Acheter sur N8N Market AI →**](https://n8nmarketai.com/products/qualification-automatique-des-leads-par-email-workflow-n8n)

> Produit numérique vendu via la boutique N8N Market AI.


> **Statut : BETA — validation/corrections en cours**

Les commerciaux perdent chaque jour plus d’une heure à trier manuellement les emails de prospects. Ce workflow identifie automatiquement les leads chauds, extrait les informations clés et notifie immédiatement le bon commercial. Les prospects reçoivent une réponse automatique tandis que tous les échanges sont tracés dans un tableau de suivi.

## Pour qui ?

Équipes commerciales et marketing qui reçoivent de nombreux leads par email et veulent gagner du temps sur le tri et la qualification.

## Prérequis

Connecter un compte Gmail (IMAP + SMTP), une clé API OpenAI et un compte Google Sheets.

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Le JSON est corrompu car il manque le noeud declencheur Email Trigger mentionne dans la description. Il faut imperativement reexporter le workflow depuis n8n en s assurant de selectionner tous les noeuds. Sans cela, le template est techniquement invalide.

## Limites

Le workflow ne lit pas les pièces jointes ni les emails en image. Il ne met pas à jour un vrai CRM comme HubSpot ou Salesforce. La classification dépend de la qualité de rédaction du prospect et peut se tromper sur des emails courts ou ambigus. Il ne gère pas les réponses aux relances, seulement les premiers contacts.

## Tags

qualification leads, triage email, lead scoring, automatisation commerciale, openai, google sheets, gmail
