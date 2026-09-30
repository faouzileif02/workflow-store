# Alerte email automatique baisse de prix Amazon

![Alerte email automatique baisse de prix Amazon](./workflow-banner.svg)

## 🛒 Acheter ce workflow

[**Acheter sur N8N Market AI →**](https://n8nmarketai.com/products/alerte-email-automatique-baisse-de-prix-amazon-workflow-n8n)

> Produit numérique vendu via la boutique N8N Market AI.


> **Statut : BETA — validation/corrections en cours**

Ne ratez plus jamais une baisse de prix sur Amazon. Ce workflow vérifie automatiquement vos produits surveillés toutes les six heures et vous envoie un email dès que le prix descend en dessous de votre seuil cible. Fini les vérifications manuelles quotidiennes et les achats trop chers.

## Pour qui ?

Acheteurs réguliers sur Amazon qui veulent être notifiés des baisses de prix sans avoir à vérifier manuellement.

## Prérequis

Connectez votre compte Google (Google Sheets + Gmail)

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Il faut remplacer le noeud HTTP Request par une API specialisee (Rainforest, Keepa) ou un proxy anti-bot car le scraping direct echouera. Il manque aussi un lien vers un template Google Sheets a cloner pour garantir la bonne structure des colonnes.

## Limites

Le workflow ne fonctionne pas si Amazon sert un captcha ou bloque l adresse IP, ce qui arrive de facon impredictible. Il ne couvre pas les prix vendeurs tiers sur la meme page. Il ne gere pas les variations de produit comme la taille ou la couleur. Le prix extrait peut etre faux si Amazon change sa structure HTML.

## Tags

amazon, alerte prix, price tracker, baisse de prix, notification email
