# Workflow Store

Catalogue privé de workflows n8n professionnels préparés pour une future commercialisation.

> Les fichiers JSON commerciaux complets ne sont pas publiés dans les fiches du catalogue. Les workflows marqués **BETA** doivent encore être corrigés/testés avant vente.

## Catalogue

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

- **19 fichiers workflow sources** dans le dossier de travail
- **17 produits uniques** dans ce catalogue
- Les 3 variantes du workflow **Tri CV → Notion** sont regroupées sous une seule fiche produit.
- **1 workflow est actuellement classé publiable** par sa fiche de validation : Alerte rupture de stock Google Sheets.
- Les autres sont conservés en **BETA** avec leur point principal à corriger avant commercialisation.

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
