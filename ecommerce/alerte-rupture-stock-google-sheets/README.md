# Alerte automatique rupture de stock Google Sheets

Workflow n8n pour petites boutiques, e-commerçants et responsables logistiques utilisant Google Sheets pour suivre leur stock.

## Ce que fait le workflow

Chaque matin à **07:00**, il :

1. lit la feuille de stock Google Sheets ;
2. compare la quantité actuelle au seuil d'alerte ;
3. détecte les produits critiques ;
4. regroupe les alertes en un seul récapitulatif ;
5. envoie un email au responsable ;
6. met à jour la colonne `Dernière alerte envoyée`.

## Applications utilisées

- n8n
- Google Sheets
- Gmail

## Structure Google Sheets requise

| produit | quantité actuelle | seuil d'alerte | Dernière alerte envoyée |
|---|---:|---:|---|
| Produit A | 5 | 10 | |
| Produit B | 25 | 8 | |

Un modèle CSV est fourni dans ce dossier.

## Fonctionnement

```text
07:00
  ↓
Google Sheets
  ↓
Analyse du stock
  ↓
Produit sous le seuil ?
  ├── NON → fin
  └── OUI
       ↓
Regroupement
       ↓
Email récapitulatif
       ↓
Mise à jour de la date d'alerte
```

## Configuration nécessaire

- instance n8n ;
- compte Google ;
- Google Sheets ;
- Gmail ;
- feuille de stock respectant les colonnes indiquées.

## Version commerciale

Le workflow JSON complet reste privé.

La version commerciale peut inclure :
- workflow n8n importable ;
- modèle Google Sheets ;
- guide d'installation ;
- personnalisation des seuils ;
- personnalisation des emails ;
- support d'installation.

## Limites

- ne déduit pas automatiquement les ventes du stock ;
- ne commande pas automatiquement chez un fournisseur ;
- ne gère pas plusieurs entrepôts ;
- dépend de données Google Sheets à jour.

## Tags

`n8n` `google-sheets` `gmail` `inventory` `stock` `automation`
