# Confirmation de rendez-vous par SMS pour salon — n8n

![Confirmation RDV automatique par SMS](./workflow-banner.svg)

> **Une automatisation pour envoyer un SMS de confirmation, traiter la réponse du client et mettre à jour le rendez-vous dans Google Sheets.**

[**🛠️ Commander une version personnalisée — 29 € →**](https://n8nmarketai.com/products/confirmation-rdv-automatique-par-sms-pour-salon-workflow-n8n)

## 🛒 Offre N8N Market AI

| | |
|---|---|
| 💰 **Prix** | **29 €** |
| 📌 **Statut** | **🟠 BETA / PERSONNALISATION** |
| 📦 **Livraison** | Finalisation + validation avant livraison |
| 🧩 **Niveau** | Intermédiaire |
| ⏱️ **Installation estimée** | 30–60 min* |
| 🔧 **Personnalisation** | Disponible |

<sub>*Estimation hors récupération, création ou validation des accès externes.</sub>

---

## Pourquoi ce produit ?

Le suivi manuel des confirmations de rendez-vous demande du temps et peut laisser des annulations sans traitement clair.

Il vise à :
- sélectionner les rendez-vous à confirmer ;
- envoyer le SMS ;
- recevoir la réponse ;
- retrouver le bon rendez-vous ;
- mettre à jour le statut ;
- alerter le gérant en cas d’annulation.

## Pour qui ?

- salons de coiffure ;
- instituts ;
- barbiers ;
- petites structures gérant les RDV dans Sheets ;
- professionnels utilisant Twilio ou un canal SMS compatible.

## Avant / Après

| Avant | Avec le workflow |
|---|---|
| Relance manuelle | SMS planifié |
| Réponse reçue sans lien clair | Recherche du RDV par numéro |
| Statut mis à jour à la main | Mise à jour automatisée |
| Annulation découverte tard | Alerte gérant |
| Un seul flux fragile | Envoi et réception séparés |

## Architecture

![Architecture Confirmation RDV automatique par SMS](./architecture-appointment-sms.svg)

**Planning → SMS sortant → Réponse client → Recherche RDV → Mise à jour**

## Fonctionnement cible

1. Identifier les rendez-vous à confirmer.
2. Envoyer le SMS via le fournisseur choisi.
3. Recevoir la réponse dans un workflow/webhook séparé.
4. Associer le numéro au bon rendez-vous.
5. Mettre à jour confirmé/annulé.
6. Notifier le gérant si nécessaire.

## Cas d'usage

### Salon de coiffure
Confirmer les rendez-vous du lendemain.

### Institut
Centraliser les réponses SMS dans le planning.

### Barbier
Être averti rapidement d’une annulation.

## ⚠️ À finaliser avant livraison

- séparer l’envoi et la réception en deux workflows adaptés ;
- lier de manière fiable le numéro entrant à la bonne ligne Sheets ;
- définir les mots de réponse acceptés ;
- tester les erreurs et numéros invalides ;
- documenter les coûts et paramètres du fournisseur SMS.

## Limites

- ne remplit pas automatiquement le créneau avec une liste d’attente ;
- les RDV doivent être disponibles dans la source configurée ;
- l’intégration avec un logiciel de caisse/réservation demande une adaptation ;
- les SMS ont un coût selon le fournisseur.

## 🛡️ Garde-fous recommandés

- opt-out et règles de consentement à prévoir ;
- journalisation des SMS ;
- aucun message si numéro invalide ;
- limite de fréquence ;
- credentials Twilio dans n8n.

## 📦 Ce que vous recevez

Après finalisation :
- workflows envoi + réception finalisés ;
- modèle Google Sheets ;
- templates SMS ;
- guide Twilio ;
- règles de statut ;
- configuration alertes.

> Le fichier JSON commercial complet, les clés API et les credentials clients ne sont pas publiés dans ce dépôt.

## Prérequis

- n8n ;
- Google Sheets ;
- fournisseur SMS compatible ;
- Gmail ou email si l’alerte gérant est conservée.

## FAQ

### Pourquoi deux workflows ?
Parce que l’envoi planifié et la réception d’un SMS entrant utilisent des déclencheurs différents.

### Peut-on changer le texte du SMS ?
Oui.

### Le créneau libéré est-il automatiquement réattribué ?
Pas dans la version standard.

### Peut-on connecter un logiciel de réservation ?
Oui, via une adaptation si une API est disponible.

---

## Automatisez les confirmations sans perdre la réponse du client

[**🛠️ Commander la version personnalisée — 29 € →**](https://n8nmarketai.com/products/confirmation-rdv-automatique-par-sms-pour-salon-workflow-n8n)

[← Retour au catalogue N8N Market AI](../../README.md)
