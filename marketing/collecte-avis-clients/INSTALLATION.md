# Installation — Collecte automatique d'avis clients après achat

## Prérequis

Connecter un compte Gmail, Typeform, Slack et Google Sheets.

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials dans n8n.
3. Remplacer les IDs, comptes, pages, feuilles et paramètres propres au client.
4. Tester avec des données de test.
5. Valider avant production : Il faut scinder le processus en deux workflows distincts car un noeud Trigger (Typeform) ne peut pas s insérer au milieu d un flux. Il faut aussi transmettre l ID de commande à Typeform via un champ caché pour lier l avis au bon client..

## Limites connues

Le workflow ne publie pas l avis sur Google My Business à la place du client, il l y invite seulement. Il ne relance pas le client qui n a pas répondu au formulaire. Il ne gère pas plusieurs commandes simultanées du même client sans doublon si le Webhook est appelé plusieurs fois. Il ne s intègre pas nativement à Shopify ou WooCommerce sans que ces plateformes envoient elles-mêmes le Webhook au bon moment.

