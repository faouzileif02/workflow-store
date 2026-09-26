# Installation — Alerte IA appels d'offres publics par email

## 1. Préparer les comptes et services

OpenAI + Gmail (ou SMTP) + Google Sheets

## 2. Importer le workflow

Importer le fichier JSON fourni dans la version commerciale dans n8n.

## 3. Configurer les credentials

Créer les credentials nécessaires directement dans n8n. Ne jamais mettre de clé API ou mot de passe en dur dans le workflow.

## 4. Adapter les identifiants et destinations

Remplacer les identifiants de démonstration, IDs de fichiers, adresses email, bases Notion, feuilles Google Sheets ou autres valeurs propres au client.

## 5. Tester avant activation

Exécuter le workflow sur des données de test et vérifier chaque branche avant de l'activer en production.

## Point à corriger avant vente

le code JSON est completement casse par des erreurs de syntaxe et doit etre nettoye de ses regex. Il manque le lien vers un template Google Sheets pret a l emploi pour le systeme anti-doublon. Le prompt OpenAI doit inclure une limite stricte de tokens pour rassurer sur les couts.

## Limites connues

Le workflow ne télécharge pas les cahiers des charges complets, il ne candidate pas automatiquement, il ne couvre pas les plateformes régionales privées hors flux RSS public, et la pertinence des alertes dépend directement de la qualité des mots-clés que l'utilisateur aura renseignés dans les paramètres. --- MISSION ARCHITECTE : Tu construis la chaîne RSS Feed Read vers OpenAI vers Gmail avec Google Sheets en mémoire anti-doublon. Le point technique à surveiller est le typeVersion du noeud RSS Feed Read, vérifie qu'il parse correctement les champs description et pubDate du flux BOAMP qui a une structure non standard, et assure-toi que le noeud Google Sheets utilise un lookup sur l'identifiant unique de l'avis avant chaque envoi pour ne jamais alerter deux fois sur la même offre. MISSION AUDITEUR : Le risque prioritaire est le coût OpenAI qui peut dériver si le flux RSS remonte cent entrées par jour et que chaque entrée est envoyée en totalité au modèle. Tu vérifies si une troncature du texte en amont du noeud OpenAI suffit à contenir le coût sous un centime par exécution, et tu identifies si le flux BOAMP impose une limite de requêtes ou un User-Agent déclaré pour ne pas être bloqué silencieusement. MISSION VENDEUR : Tu vises l'artisan du bâtiment et le consultant indépendant qui ont déjà perdu un marché faute d'information à temps, pas le directeur achat d'une PME qui a déjà ses outils. L'angle de vente est l'économie concrète, une seule offre trouvée vaut des milliers d'euros de chiffre d'affaires, et le template remplace un abonnement SaaS à trois cents euros par an par zéro euro et dix minutes de configuration. MISSION COMMUNAUTE : Est-ce que tu installerais ce workflow aujourd'hui si la configuration se limitait à coller tes cinq mots-clés métier dans un champ texte et à connecter ton Gmail, ou est-ce que le fait de payer quelques centimes OpenAI par semaine te ferait abandonner l'installation avant même de la tester ?

