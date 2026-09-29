# Prospection IA — Brouillons Gmail — n8n

**Statut :** LIVRABLE  
**Version de référence :** V5.1

## Objectif

Automatiser la prospection B2B : recherche de prospects, résolution du site officiel, recherche d'emails publics, qualification IA, création de brouillons Gmail et journalisation Google Sheets.

## Fichier livré

- `N8N-Market-AI-Prospection-AUDITE-V5.1.json`

## Points validés

- score minimum de qualification : 75/100 ;
- priorité aux prospects avec site web ;
- exclusion des centres commerciaux, hypermarchés et profils peu pertinents ;
- blocage des annuaires parasites ;
- clé SerpAPI via `SERPAPI_API_KEY` ;
- brouillons Gmail uniquement, sans envoi automatique ;
- déduplication et journalisation Google Sheets.

## Sécurité

Aucune clé API ne doit être stockée en clair dans GitHub. Les credentials sont configurés directement dans n8n.
