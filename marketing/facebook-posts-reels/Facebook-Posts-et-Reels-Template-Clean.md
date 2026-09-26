# Facebook Posts + Reels — n8n

Généré et nettoyé le 2026-09-26 pour le catalogue Workflow Store.

## Validation de structure

Structure conservée avec **24 nœuds**.

Le workflow contient deux branches indépendantes :

1. **Publication Facebook normale** avec image + texte.
2. **Publication Facebook Reel** avec vidéo + texte.

Le fichier JSON complet reste privé et n'est pas publié sur GitHub.

## Fiche de publication

**TITRE :**  
Publication automatique Facebook Posts + Reels avec IA

**DESCRIPTION :**  
Ce workflow n8n automatise deux types de contenu Facebook dans un seul projet. Une première branche publie des posts classiques avec image et texte généré par IA. Une seconde branche publie des Reels avec vidéo et texte généré par IA. Le workflow peut également utiliser la météo du jour, faire tourner automatiquement les médias et mémoriser les derniers médias utilisés afin d'éviter les répétitions.

**POUR QUI :**  
Petites entreprises locales, indépendants, agences, community managers et commerces qui veulent automatiser leurs publications Facebook et leurs Reels depuis n8n.

**SETUP :**  
Configurer dans n8n :
- Facebook Graph API / Page Facebook ;
- OpenRouter ou un autre modèle IA compatible ;
- Data Tables n8n pour mémoriser les images et vidéos utilisées ;
- les listes d'images et de vidéos du client.

**TAGS :**  
facebook, reels, n8n, automatisation, publication automatique, intelligence artificielle, social media, marketing

## Fonctionnement

### Branche 1 — Reel Facebook

Déclenchement prévu :
- 09:00
- 12:30
- 18:30

Flux :

```text
Post reel
→ Config
→ Météo
→ Dernière vidéo utilisée
→ Choix d'une vidéo
→ Préparer contexte
→ Rédaction IA
→ Parser et valider
→ Publier Reel sur Facebook
→ Enregistrer la vidéo postée
```

### Branche 2 — Post Facebook normal

Déclenchement prévu :
- 09:00
- 12:00
- 18:00

Flux :

```text
Heure de post
→ Config
→ Météo
→ Dernière image utilisée
→ Choix d'une image
→ Préparer contexte
→ Rédaction IA
→ Parser et valider
→ Publier sur Facebook
→ Enregistrer l'image postée
```

## Fonctions principales

- Publication automatique de posts Facebook avec image.
- Publication automatique de Reels Facebook avec vidéo.
- Génération de texte par IA.
- Rotation automatique des images.
- Rotation automatique des vidéos.
- Mémoire du dernier média utilisé.
- Utilisation facultative de la météo.
- Gestion d'une promotion depuis la configuration.
- Parsing et validation du texte généré avant publication.
- Hashtags et appel à l'action générés automatiquement.

## Version nettoyée

La version destinée au catalogue a été nettoyée.

Elle ne contient pas :
- credentials enregistrés ;
- clés API ;
- tokens ;
- identifiant personnel de page Facebook ;
- identifiants Data Table propres à l'instance d'origine ;
- instanceId n8n.

Les nœuds de connexion restent présents mais doivent être configurés par l'utilisateur après import.

## Configuration nécessaire après import

1. Sélectionner ou créer les Data Tables pour les images et vidéos.
2. Renseigner l'identifiant de la Page Facebook.
3. Connecter Facebook Graph API dans les credentials n8n.
4. Connecter le modèle IA choisi.
5. Remplacer les médias d'exemple par les URLs d'images et vidéos du client.
6. Adapter le texte, la localisation et la promotion à l'entreprise.
7. Tester séparément la branche Post et la branche Reel avant activation.

## Limites

- La publication dépend des autorisations accordées par Meta à la Page Facebook.
- Les médias doivent être accessibles depuis une URL compatible avec Facebook.
- Les Data Tables doivent être créées dans l'instance n8n de l'utilisateur.
- Le modèle IA et les credentials doivent être configurés séparément.
- Les horaires sont des valeurs par défaut et peuvent être modifiés.

## Statut

**TEMPLATE PRIVÉ — prêt à configurer et tester.**

Le JSON complet est conservé dans Google Drive et n'est pas accessible publiquement depuis le dépôt GitHub.