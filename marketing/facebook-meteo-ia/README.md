# Publication Facebook auto météo locale + IA pour Hannut

> **Statut : BETA — validation/corrections en cours**

Faouzi n’a plus à rédiger chaque matin le post de sa page de nettoyage auto. Le workflow récupère la météo réelle à Hannut, génère un texte adapté à la pluie ou au soleil, et publie automatiquement avec une image du jour. Il résout le manque d’inspiration et de pertinence locale qui touche tous les indépendants qui gèrent seuls leur présence sur Facebook.

## Pour qui ?

Gérants de petites entreprises locales (nettoyage auto, carrosserie, lavage) en Belgique qui veulent des posts pertinents sans y passer du temps chaque jour.

## Prérequis

Connecter Google Sheets, OpenWeatherMap, OpenAI, Facebook Graph API et un compte email.

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

Il faut remplacer la requete HTTP API Graph par le noeud natif Facebook de n8n qui gere l OAuth2 simplement. Si l API brute est maintenue, il manque un tutoriel video pas a pas obligatoire pour generer le token.

## Limites

le workflow ne vérifie pas si l'image Imgur est encore accessible au moment de la publication. Il ne gère pas les commentaires ni les réponses aux abonnés. Il ne détecte pas si Facebook a rejeté le post pour cause de contenu ou de limite d'API. Le token Facebook devra être rafraîchi manuellement tous les 60 jours environ, sans quoi les publications s'arrêtent. --- MISSION ARCHITECTE : construis le noeud IF de fallback météo en priorité absolue avant de brancher OpenAI. Le point technique à surveiller est la structure exacte du JSON retourné par OpenWeatherMap, notamment le champ weather[0].description qui peut être absent si la ville n'est pas reconnue. Si ce champ est vide ou null, le Set de fallback doit injecter la chaîne "temps variable" pour que le prompt OpenAI reçoive toujours une valeur valide et ne génère jamais un post avec une variable non résolue visible dans le texte publié. MISSION AUDITEUR : ton risque prioritaire est la limite de l'API Facebook Graph sur la publication d'images via URL externe. Vérifie si le endpoint /page-id/photos accepte un paramètre url pointant vers Imgur sans que Facebook ne télécharge et rejette l'image pour format ou taille non conformes. Vérifie également le quota gratuit d'OpenWeatherMap en appels par minute pour confirmer qu'un appel unique par jour ne risque pas d'être bloqué par un rate limit partagé avec d'autres utilisateurs du même compte. MISSION VENDEUR : vise les groupes Facebook et forums dédiés aux indépendants du secteur automobile et de la carrosserie en Belgique et dans le nord de la France. L'angle de vente est concret et local : ton post Facebook parle de pluie quand il pleut à Hannut, pas d'un soleil imaginaire. Mets en avant le fait que c'est le seul template n8n qui connecte la météo locale au contenu publié pour un commerce de proximité, et que la communauté des créateurs de templates confirme que ça n'existe pas encore. MISSION COMMUNAUTE : est-ce qu'un gérant de petite entreprise locale qui n'a jamais touché à n8n peut configurer ce workflow en moins d'une heure avec uniquement le README fourni, ou bien l'étape de création du token Facebook Graph longue durée est-elle un point de blocage suffisant pour qu'il abandonne avant la première publication ?

## Tags

facebook, meteo, openai, publication auto, hannut, automobile, contenu local
