# Installation — Compte-rendu Zoom vers Notion avec prompt personnalisé

## 1. Préparer les comptes et services

Connectez votre compte Zoom (webhook + Cloud Recordings), OpenAI et Notion.

## 2. Importer le workflow

Importer le fichier JSON fourni dans la version commerciale dans n8n.

## 3. Configurer les credentials

Créer les credentials nécessaires directement dans n8n. Ne jamais mettre de clé API ou mot de passe en dur dans le workflow.

## 4. Adapter les identifiants et destinations

Remplacer les identifiants de démonstration, IDs de fichiers, adresses email, bases Notion, feuilles Google Sheets ou autres valeurs propres au client.

## 5. Tester avant activation

Exécuter le workflow sur des données de test et vérifier chaque branche avant de l'activer en production.

## Point à corriger avant vente

Le code JSON du workflow est totalement absent. Il faut absolument integrer un decoupage du fichier audio car le simple avertissement propose pour la limite de 25 Mo de Whisper rendra le template inoperant. Il manque aussi un systeme de retry pour palier le delai de disponibilite du fichier chez Zoom.

## Limites connues

le workflow ne fonctionne qu'avec les enregistrements Zoom Cloud, pas les enregistrements locaux. Il ne détecte pas automatiquement les locuteurs, la diarisation n'est pas incluse. Il ne relit pas les pages Notion existantes ni ne fusionne plusieurs réunions. Le coût Whisper d'environ 0,006 dollar par minute audio est réel et à la charge de l'utilisateur. --- MISSION ARCHITECTE : construis le noeud httpRequest de téléchargement du fichier Zoom avec soin, car Zoom exige un header Authorization Bearer avec le token OAuth et redirige vers une URL signée S3 — tu dois activer le suivi de redirection et gérer le binaire correctement pour que le noeud OpenAI Whisper reçoive un fichier valide et non une réponse JSON d'erreur silencieuse. Surveille la limite de taille de fichier Whisper fixée à vingt-cinq mégaoctets et prévois un noeud conditionnel qui avertit l'utilisateur si le fichier dépasse ce seuil plutôt que de laisser l'appel échouer sans message clair. MISSION AUDITEUR : vérifie en priorité ce qui se passe quand Zoom envoie le webhook avant que le fichier d'enregistrement soit réellement disponible en téléchargement, ce qui arrive régulièrement sur les réunions longues — le httpRequest retourne alors une erreur 403 ou un fichier vide et le workflow s'arrête sans trace exploitable. Vérifie aussi que le token OAuth Zoom utilisé pour télécharger le fichier a bien le scope cloud_recording:read:admin et qu'il est rafraîchi automatiquement, car un token expiré produit exactement le même symptôme qu'un fichier indisponible et sera impossible à diagnostiquer pour un utilisateur non technique. MISSION VENDEUR : adresse-toi aux consultants indépendants et aux agences de moins de dix personnes qui facturent au temps et qui peuvent calculer immédiatement combien leur coûte chaque heure de retranscription manuelle — l'angle n'est pas l'IA ni l'automatisation, c'est le contrôle total sur le format du compte-rendu que Fathom et Fireflies leur refusent. Le titre doit contenir les mots compte-rendu et Notion, pas transcription ni IA, parce que c'est ce que ces profils tapent quand ils cherchent une solution. MISSION COMMUNAUTE : tu as dit que le marché manque d'un template n8n offrant le contrôle total du prompt — confirme ou infirme ceci

