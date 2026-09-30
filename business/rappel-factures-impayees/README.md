# Rappel automatique de factures impayées par email

![Rappel automatique de factures impayées par email](./workflow-banner.svg)

## 🛒 Offre N8N Market AI

| | |
|---|---|
| 💰 **Prix** | **29 €** |
| 📌 **Statut** | **🟠 BETA / PERSONNALISATION** |
| 📦 **Livraison** | Personnalisation + validation avant livraison |
| 🧩 **Niveau** | Facile à intermédiaire |
| ⏱️ **Installation estimée** | 20–40 min* |
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


Ce workflow détecte chaque matin les factures en retard dans votre Google Sheets et envoie automatiquement le bon niveau de relance par email selon l’ancienneté du retard. Il adapte le ton du message en fonction du palier (relance douce, ferme ou avertissement final) et tient à jour la date de dernière relance. Vous n’avez plus à chasser manuellement vos clients.

## Pour qui ?

Freelances et petites entreprises qui veulent automatiser leurs relances clients sans perdre de temps.

## Prérequis

Connexion Google account (Google Sheets + Gmail)

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Il faut filtrer la date de derniere relance pour espacer les envois, sinon le client est harcele chaque matin. Ajouter un noeud Aggregate avant le recapitulatif pour eviter d inonder la boite mail du proprietaire. Inclure un lien vers un template Google Sheets pret a l emploi.

## Limites

Le workflow ne detecte pas les paiements partiels, le statut Paye doit etre mis a jour manuellement dans la feuille. Il n integre pas de portail de paiement en ligne. Il ne gere pas les devises multiples. Il n envoie pas de SMS ni de courrier postal.

## Tags

relance facture, rappel paiement, automatisation freelance, recouvrement, google sheets

---

[← Retour au catalogue N8N Market AI](../../README.md)
