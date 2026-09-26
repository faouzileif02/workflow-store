# Installation — Alerte email automatique baisse de prix Amazon

## Prérequis

Connectez votre compte Google (Google Sheets + Gmail)

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les IDs, feuilles, comptes et paramètres de démonstration.
4. Tester avec des données de test.
5. Valider avant production : Il faut remplacer le noeud HTTP Request par une API specialisee (Rainforest, Keepa) ou un proxy anti-bot car le scraping direct echouera. Il manque aussi un lien vers un template Google Sheets a cloner pour garantir la bonne structure des colonnes..

## Limites connues

Le workflow ne fonctionne pas si Amazon sert un captcha ou bloque l adresse IP, ce qui arrive de facon impredictible. Il ne couvre pas les prix vendeurs tiers sur la meme page. Il ne gere pas les variations de produit comme la taille ou la couleur. Le prix extrait peut etre faux si Amazon change sa structure HTML.

