# Installation — Sauvegarde automatique factures et devis vers Google Drive

## 1. Préparer les comptes et services

Connecter un compte Google (Gmail + Drive) et un compte OpenAI.

## 2. Importer le workflow

Importer le fichier JSON fourni dans la version commerciale dans n8n.

## 3. Configurer les credentials

Créer les credentials nécessaires directement dans n8n. Ne jamais mettre de clé API ou mot de passe en dur dans le workflow.

## 4. Adapter les identifiants et destinations

Remplacer les identifiants de démonstration, IDs de fichiers, adresses email, bases Notion, feuilles Google Sheets ou autres valeurs propres au client.

## 5. Tester avant activation

Exécuter le workflow sur des données de test et vérifier chaque branche avant de l'activer en production.

## Point à corriger avant vente

L extraction du texte des PDF avant l analyse IA est indispensable. Se baser uniquement sur le sujet de l email produira des erreurs car les emails de facturation sont souvent generiques. Il manque aussi la boucle explicite dans les etapes pour gerer les pieces jointes multiples.

## Limites connues

Le workflow ne traite pas les pièces jointes intégrées dans le corps HTML de l'email. Il ne fusionne pas les doublons si un même document arrive deux fois. Il ne lit pas le contenu du PDF pour en extraire le montant ou le numéro de facture. Il ne fonctionne pas sur Outlook sans reconfiguration complète des credentials. --- MISSION ARCHITECTE : Tu construis le workflow en partant du Gmail Trigger typeVersion 2 et tu confirmes que le noeud Google Drive en action upload accepte bien un binaire passé depuis le noeud Gmail sans noeud intermédiaire Move Binary Data — si ce n'est pas le cas, tu insères ce noeud et tu documentes pourquoi dans les notes du workflow. Le point critique est la boucle sur les pièces jointes multiples : un email peut en contenir cinq, tu dois utiliser un SplitInBatches ou un loop explicite pour traiter chaque fichier séparément sans perdre les métadonnées de l'email parent. MISSION AUDITEUR : Ton risque prioritaire est le quota Gmail Trigger en environnement n8n self-hosted : vérifie combien d'appels API Google OAuth2 le trigger génère par heure et si un comptable qui reçoit cent emails par jour risque d'atteindre la limite des 250 unités de quota Google Workspace avant midi. Vérifie aussi ce qui se passe si OpenAI renvoie une réponse mal formée — le noeud Google Drive ne doit jamais recevoir un nom de fichier vide ou null, il faut un fallback explicite dans le noeud Code. MISSION VENDEUR : Tu vises le comptable indépendant et l'assistante de direction, pas le développeur. L'angle de vente est la peur de perdre une facture et de rater une déduction fiscale, pas la productivité abstraite. Le titre doit contenir les mots factures et Drive, et la description doit ouvrir sur une situation concrète : vous cherchez une facture depuis vingt minutes dans votre boîte Gmail, elle est quelque part, vous ne la trouvez plus. Évite tout vocabulaire technique dans les deux premières phrases. MISSION COMMUNAUTE : Est-ce que le renommage automatique produit par l'IA vous donnerait confiance pour retrouver un fichier six mois plus tard sans jamais ouvrir Google Drive manuellement, ou est-ce que vous iriez quand même vérifier chaque semaine que le tri est correct ?

