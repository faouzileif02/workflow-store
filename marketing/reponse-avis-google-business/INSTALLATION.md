# Installation — Réponse automatique aux avis Google My Business

## Prérequis

Connecter un compte Google Cloud (API Google My Business), une clé OpenAI et un compte Gmail.

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les IDs, feuilles, comptes et paramètres de démonstration.
4. Tester avec des données de test.
5. Valider avant production : La description promet un email quotidien mais le déclencheur est horaire et l email est envoyé dans la boucle. Il faut utiliser un noeud Loop, agréger les données à la fin avant Gmail et ajouter une étape d approbation humaine..

## Limites connues

Le workflow ne détecte pas le sarcasme ou l'ironie dans les avis, il ne gère pas les signalements d'avis frauduleux, il ne personnalise pas les réponses avec l'historique client, et il ne traduit pas automatiquement les avis rédigés dans une langue étrangère.

