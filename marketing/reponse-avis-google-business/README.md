# Réponse automatique aux avis Google My Business

![Réponse automatique aux avis Google My Business](./workflow-banner.svg)

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


Les petits commerces reçoivent régulièrement des avis Google qu’ils n’ont jamais le temps de traiter. Ce workflow récupère automatiquement les nouveaux avis, génère une réponse adaptée et personnalisée grâce à l’IA, puis la publie directement sur Google. Le gérant reçoit chaque jour un email récapitulatif de toutes les réponses publiées.

## Pour qui ?

Gérants de petits commerces, restaurants, artisans et boutiques locales qui souhaitent maintenir une bonne e-réputation sans y passer du temps.

## Prérequis

Connecter un compte Google Cloud (API Google My Business), une clé OpenAI et un compte Gmail.

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

La description promet un email quotidien mais le déclencheur est horaire et l email est envoyé dans la boucle. Il faut utiliser un noeud Loop, agréger les données à la fin avant Gmail et ajouter une étape d approbation humaine.

## Limites

Le workflow ne détecte pas le sarcasme ou l'ironie dans les avis, il ne gère pas les signalements d'avis frauduleux, il ne personnalise pas les réponses avec l'historique client, et il ne traduit pas automatiquement les avis rédigés dans une langue étrangère.

## Tags

Google My Business, avis clients, réponse automatique, OpenAI, e-réputation

---

[← Retour au catalogue N8N Market AI](../../README.md)
