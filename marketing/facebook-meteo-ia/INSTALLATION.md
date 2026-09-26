# Installation — Publication Facebook auto météo locale + IA pour Hannut

## Prérequis

Connecter Google Sheets, OpenWeatherMap, OpenAI, Facebook Graph API et un compte email.

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials dans n8n.
3. Remplacer les IDs, comptes, pages, feuilles et paramètres propres au client.
4. Tester avec des données de test.
5. Valider avant production : Il faut remplacer la requete HTTP API Graph par le noeud natif Facebook de n8n qui gere l OAuth2 simplement. Si l API brute est maintenue, il manque un tutoriel video pas a pas obligatoire pour generer le token..

## Limites connues

le workflow ne vérifie pas si l'image Imgur est encore accessible au moment de la publication. Il ne gère pas les commentaires ni les réponses aux abonnés. Il ne détecte pas si Facebook a rejeté le post pour cause de contenu ou de limite d'API. Le token Facebook devra être rafraîchi manuellement tous les 60 jours environ, sans quoi les publications s'arrêtent. --- MISSION ARCHITECTE : construis le noeud IF de fallback météo en priorité absolue avant de brancher OpenAI. Le point technique à surveiller est la structure exacte du JSON retourné par OpenWeatherMap, notamment le champ weather[0].description qui peut être absent si la ville n'est pas reconnue. Si ce champ est vide ou null, le Set de fallback doit injecter la chaîne "temps variable" pour que le prompt OpenAI reçoive toujours une valeur valide et ne génère jamais un post avec une variable non résolue visible dans le texte publié. MISSION AUDITEUR : ton risque prioritaire est la limite de l'API Facebook Graph sur la publication d'images via URL externe. Vérifie si le endpoint /page-id/photos accepte un paramètre url pointant vers Imgur sans que Facebook ne télécharge et rejette l'image pour format ou taille non conformes. Vérifie également le quota gratuit d'OpenWeatherMap en appels par minute pour confirmer qu'un appel unique par jour ne risque pas d'être bloqué par un rate limit partagé avec d'autres utilisateurs du même compte. MISSION VENDEUR : vise les groupes Facebook et forums dédiés aux indépendants du secteur automobile et de la carrosserie en Belgique et dans le nord de la France. L'angle de vente est concret et local : ton post Facebook parle de pluie quand il pleut à Hannut, pas d'un soleil imaginaire. Mets en avant le fait que c'est le seul template n8n qui connecte la météo locale au contenu publié pour un commerce de proximité, et que la communauté des créateurs de templates confirme que ça n'existe pas encore. MISSION COMMUNAUTE : est-ce qu'un gérant de petite entreprise locale qui n'a jamais touché à n8n peut configurer ce workflow en moins d'une heure avec uniquement le README fourni, ou bien l'étape de création du token Facebook Graph longue durée est-elle un point de blocage suffisant pour qu'il abandonne avant la première publication ?

