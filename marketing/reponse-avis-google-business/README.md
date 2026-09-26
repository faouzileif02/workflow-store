# Réponse automatique aux avis Google My Business

> **Statut : BETA — validation/corrections en cours**

Les petits commerces reçoivent régulièrement des avis Google qu’ils n’ont jamais le temps de traiter. Ce workflow récupère automatiquement les nouveaux avis, génère une réponse adaptée et personnalisée grâce à l’IA, puis la publie directement sur Google. Le gérant reçoit chaque jour un email récapitulatif de toutes les réponses publiées.

## Pour qui ?

Gérants de petits commerces, restaurants, artisans et boutiques locales qui souhaitent maintenir une bonne e-réputation sans y passer du temps.

## Prérequis

Connecter un compte Google Cloud (API Google My Business), une clé OpenAI et un compte Gmail.

## Version commerciale

Le JSON n8n complet reste privé. Le pack commercial comprendra le workflow importable, la documentation et les paramètres de configuration.

## Point à corriger avant commercialisation

La description promet un email quotidien mais le déclencheur est horaire et l email est envoyé dans la boucle. Il faut utiliser un noeud Loop, agréger les données à la fin avant Gmail et ajouter une étape d approbation humaine.

## Limites

Le workflow ne détecte pas le sarcasme ou l'ironie dans les avis, il ne gère pas les signalements d'avis frauduleux, il ne personnalise pas les réponses avec l'historique client, et il ne traduit pas automatiquement les avis rédigés dans une langue étrangère.

## Tags

Google My Business, avis clients, réponse automatique, OpenAI, e-réputation
