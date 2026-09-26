# Installation — Rappel automatique de factures impayées par email

## Prérequis

Connexion Google account (Google Sheets + Gmail)

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials dans n8n.
3. Remplacer les IDs, feuilles, emails et paramètres de démonstration.
4. Exécuter des tests sur des données de test.
5. Corriger/valider le point suivant avant production : Il faut filtrer la date de derniere relance pour espacer les envois, sinon le client est harcele chaque matin. Ajouter un noeud Aggregate avant le recapitulatif pour eviter d inonder la boite mail du proprietaire. Inclure un lien vers un template Google Sheets pret a l emploi..

## Limites connues

Le workflow ne detecte pas les paiements partiels, le statut Paye doit etre mis a jour manuellement dans la feuille. Il n integre pas de portail de paiement en ligne. Il ne gere pas les devises multiples. Il n envoie pas de SMS ni de courrier postal.

