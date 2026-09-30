# Compte-rendu Zoom vers Notion avec prompt personnalisé

![Compte-rendu Zoom vers Notion avec prompt personnalisé](./workflow-banner.svg)

> **Statut : BETA — validation/corrections en cours**

Les consultants et chefs de projet perdent jusqu’à deux heures après chaque appel client à retranscrire, structurer et mettre en page un compte-rendu exploitable. Les outils SaaS comme Fathom ou Fireflies produisent un format figé sans aucun contrôle sur la structure ni sur le prompt. Ce workflow crée automatiquement une page Notion parfaitement structurée selon votre propre prompt et votre mise en page exacte.

## Pour qui ?

Consultants indépendants et petites agences qui facturent au temps et qui veulent arrêter de perdre des heures sur les comptes-rendus.

## Prérequis

Connectez votre compte Zoom (webhook + Cloud Recordings), OpenAI et Notion.

## Version commerciale

Le fichier JSON n8n complet reste privé. Le pack commercial est prévu pour inclure le workflow importable, les instructions de configuration et les paramètres à personnaliser.

## Validation technique

La fiche de conception indique que ce workflow doit encore être retravaillé avant commercialisation.

**Point principal à corriger :** Le code JSON du workflow est totalement absent. Il faut absolument integrer un decoupage du fichier audio car le simple avertissement propose pour la limite de 25 Mo de Whisper rendra le template inoperant. Il manque aussi un systeme de retry pour palier le delai de disponibilite du fichier chez Zoom.

## Limites

le workflow ne fonctionne qu'avec les enregistrements Zoom Cloud, pas les enregistrements locaux. Il ne détecte pas automatiquement les locuteurs, la diarisation n'est pas incluse. Il ne relit pas les pages Notion existantes ni ne fusionne plusieurs réunions. Le coût Whisper d'environ 0,006 dollar par minute audio est réel et à la charge de l'utilisateur. --- MISSION ARCHITECTE : construis le noeud httpRequest de téléchargement du fichier Zoom avec soin, car Zoom exige un header Authorization Bearer avec le token OAuth et redirige vers une URL signée S3 — tu dois activer le suivi de redirection et gérer le binaire correctement pour que le noeud OpenAI Whisper reçoive un fichier valide et non une réponse JSON d'erreur silencieuse. Surveille la limite de taille de fichier Whisper fixée à vingt-cinq mégaoctets et prévois un noeud conditionnel qui avertit l'utilisateur si le fichier dépasse ce seuil plutôt que de laisser l'appel échouer sans message clair. MISSION AUDITEUR : vérifie en priorité ce qui se passe quand Zoom envoie le webhook avant que le fichier d'enregistrement soit réellement disponible en téléchargement, ce qui arrive régulièrement sur les réunions longues — le httpRequest retourne alors une erreur 403 ou un fichier vide et le workflow s'arrête sans trace exploitable. Vérifie aussi que le token OAuth Zoom utilisé pour télécharger le fichier a bien le scope cloud_recording:read:admin et qu'il est rafraîchi automatiquement, car un token expiré produit exactement le même symptôme qu'un fichier indisponible et sera impossible à diagnostiquer pour un utilisateur non technique. MISSION VENDEUR : adresse-toi aux consultants indépendants et aux agences de moins de dix personnes qui facturent au temps et qui peuvent calculer immédiatement combien leur coûte chaque heure de retranscription manuelle — l'angle n'est pas l'IA ni l'automatisation, c'est le contrôle total sur le format du compte-rendu que Fathom et Fireflies leur refusent. Le titre doit contenir les mots compte-rendu et Notion, pas transcription ni IA, parce que c'est ce que ces profils tapent quand ils cherchent une solution. MISSION COMMUNAUTE : tu as dit que le marché manque d'un template n8n offrant le contrôle total du prompt — confirme ou infirme ceci

## Tags

compte-rendu, notion, zoom, prompt personnalisé, réunion
