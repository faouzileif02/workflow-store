# Sauvegarde automatique factures et devis vers Google Drive

![Sauvegarde automatique factures et devis vers Google Drive](./workflow-banner.svg)

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


Vous cherchez une facture depuis vingt minutes dans votre boîte Gmail, elle est quelque part, vous ne la trouvez plus. Chaque semaine des dizaines de pièces jointes s’éparpillent entre votre boîte mail et vos téléchargements. Ce workflow les renomme automatiquement avec l’IA et les range dans des dossiers fournisseurs sans que vous ayez à toucher quoi que ce soit.

## Pour qui ?

Comptables indépendants, assistantes de direction et prestataires qui reçoivent de nombreuses factures et devis par email.

## Prérequis

Connecter un compte Google (Gmail + Drive) et un compte OpenAI.

## Version commerciale

Le fichier JSON n8n complet reste privé. Le pack commercial est prévu pour inclure le workflow importable, les instructions de configuration et les paramètres à personnaliser.

## Validation technique

La fiche de conception indique que ce workflow doit encore être retravaillé avant commercialisation.

**Point principal à corriger :** L extraction du texte des PDF avant l analyse IA est indispensable. Se baser uniquement sur le sujet de l email produira des erreurs car les emails de facturation sont souvent generiques. Il manque aussi la boucle explicite dans les etapes pour gerer les pieces jointes multiples.

## Limites

Le workflow ne traite pas les pièces jointes intégrées dans le corps HTML de l'email. Il ne fusionne pas les doublons si un même document arrive deux fois. Il ne lit pas le contenu du PDF pour en extraire le montant ou le numéro de facture. Il ne fonctionne pas sur Outlook sans reconfiguration complète des credentials.

## Tags

factures, google drive, renommage ia, tri automatique, fournisseurs

---

[← Retour au catalogue N8N Market AI](../../README.md)
