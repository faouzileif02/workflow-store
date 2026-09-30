# Collecte automatique d'avis clients après achat

![Collecte automatique d'avis clients après achat](./workflow-banner.svg)

## 🛒 Offre N8N Market AI

| | |
|---|---|
| 💰 **Prix** | **29 €** |
| 📌 **Statut** | **🟠 BETA / PERSONNALISATION** |
| 📦 **Livraison** | Personnalisation + validation avant livraison |
| 🧩 **Niveau** | Intermédiaire |
| ⏱️ **Installation estimée** | 30–60 min* |
| 🔧 **Personnalisation** | Disponible |

[**🛠️ Commander une version personnalisée →**](https://n8nmarketai.com/)

<sub>*Estimation hors création, validation ou récupération des accès aux services externes.</sub>

## 📦 Ce que vous recevez

- une version finalisée et adaptée à votre environnement ;
- le workflow n8n importable après validation ;
- le guide de configuration ;
- la liste des comptes, API et credentials à connecter ;
- les paramètres à personnaliser.

> Le fichier JSON commercial complet, les clés API et les credentials clients ne sont pas publiés sur GitHub.


Ce workflow envoie automatiquement une demande d'avis au bon moment, 24h après la livraison ou la fin de la prestation. Il trie les réponses selon la note donnée : il invite les clients satisfaits à laisser un avis public sur Google et alerte immédiatement l'équipe en cas d'insatisfaction. Toutes les réponses sont enregistrées dans Google Sheets avec le statut correspondant.

## Pour qui ?

Commerçants, e-commerçants, prestataires de services et responsables SAV qui veulent collecter plus d'avis tout en détectant rapidement les clients mécontents.

## Prérequis

Connecter un compte Gmail, Typeform, Slack et Google Sheets.

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Il faut scinder le processus en deux workflows distincts car un noeud Trigger (Typeform) ne peut pas s insérer au milieu d un flux. Il faut aussi transmettre l ID de commande à Typeform via un champ caché pour lier l avis au bon client.

## Limites

Le workflow ne publie pas l avis sur Google My Business à la place du client, il l y invite seulement. Il ne relance pas le client qui n a pas répondu au formulaire. Il ne gère pas plusieurs commandes simultanées du même client sans doublon si le Webhook est appelé plusieurs fois. Il ne s intègre pas nativement à Shopify ou WooCommerce sans que ces plateformes envoient elles-mêmes le Webhook au bon moment.

## Tags

avis clients, collecte automatique, satisfaction client, Typeform, Google Sheets

---

[← Retour au catalogue N8N Market AI](../../README.md)
