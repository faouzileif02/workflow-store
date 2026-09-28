# Workflow Store

Catalogue privé de workflows n8n professionnels préparés pour une future commercialisation.

> Les fichiers JSON commerciaux complets ne sont pas publiés dans les fiches du catalogue. Les workflows marqués **BETA** doivent encore être corrigés/testés avant vente.

## Catalogue

### Agents IA — Marketing & Ventes
- [Agent IA Directeur Marketing & Ventes](agents-ia/directeur-marketing-ventes/) — **EN DÉVELOPPEMENT**
- [Agent IA SEO & Fiches Produits](agents-ia/seo-fiches-produits/) — **EN DÉVELOPPEMENT**
- [Agent IA Conversion & Relance Clients](agents-ia/conversion-relance-clients/) — **EN DÉVELOPPEMENT**

### Business
- [Création auto fiche client Google Sheets + alerte Slack](business/fiche-client-google-sheets-slack/)
- [Qualification automatique des leads par email](business/qualification-leads-email/)
- [Rappel automatique de factures impayées par email](business/rappel-factures-impayees/)
- [Sauvegarde automatique factures et devis vers Google Drive](business/sauvegarde-factures-devis-google-drive/)

### Productivité
- [Alerte IA appels d'offres publics par email](productivity/appels-offres-ia-email/)
- [Compte-rendu Zoom vers Notion avec prompt personnalisé](productivity/zoom-notion-prompt/)

### Marketing
- [Collecte automatique d'avis clients après achat](marketing/collecte-avis-clients/)
- [Publication Facebook auto météo locale + IA](marketing/facebook-meteo-ia/)
- [Facebook Posts + Reels — image + vidéo](marketing/facebook-posts-reels/)
- [Publication automatique LinkedIn depuis Google Sheets](marketing/linkedin-google-sheets/)
- [Réponse automatique aux avis Google Business](marketing/reponse-avis-google-business/)
- [Veille marque automatique Twitter/X vers Google Sheets](marketing/veille-marque-twitter-google-sheets/)

### E-commerce
- [Alerte email automatique baisse de prix Amazon](ecommerce/alerte-baisse-prix-amazon/)
- [Alerte automatique rupture de stock Google Sheets](ecommerce/alerte-rupture-stock-google-sheets/) — **PUBLIABLE**

### RH
- [Accusé réception automatique candidature formulaire](rh/accuse-reception-candidature/)
- [Tri automatique des CV par email vers Notion](rh/tri-cv-email-notion/) — 3 variantes regroupées

### Salon — workflows indépendants
- [Confirmation RDV automatique par SMS](salon/confirmation-rdv-sms/)
- [Relance SMS automatique clients inactifs 90 jours](salon/relance-clients-inactifs-90j/)

## État du catalogue

- **18 workflows existants** documentés dans le catalogue principal.
- **3 nouveaux Agents IA Marketing & Ventes** ajoutés en phase de conception.
- **21 fiches catalogue au total** dans le dépôt après cet ajout.
- Les 3 variantes du workflow **Tri CV → Notion** sont regroupées sous une seule fiche produit.
- **1 workflow est actuellement classé publiable** par sa fiche de validation : Alerte rupture de stock Google Sheets.
- Les autres workflows existants sont conservés en **BETA** avec leur point principal à corriger avant commercialisation.
- Les 3 Agents IA sont en **EN DÉVELOPPEMENT** : architecture documentée, JSON n8n à construire et tester avant vente.

## Structure d'une fiche produit

Chaque produit contient au minimum :

- `README.md` — présentation, cible, prérequis et limites ;
- `INSTALLATION.md` — étapes de configuration et de test ;
- éventuellement un modèle ou un fichier de variantes.

## Sécurité

Les clés API, mots de passe, credentials OAuth et secrets ne doivent jamais être stockés dans les fichiers publics ou directement dans les workflows. Ils doivent être configurés via les credentials n8n.

---

**Projet :** Workflow Store  
**Plateforme :** n8n
