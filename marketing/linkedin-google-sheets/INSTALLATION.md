# Installation — Publication automatique LinkedIn depuis Google Sheets

## Prérequis

Connecter un compte Google (Sheets + Gmail) et un compte LinkedIn Personal (OAuth).

## Étapes

1. Importer le JSON commercial dans n8n.
2. Configurer les credentials nécessaires.
3. Remplacer les IDs, feuilles, comptes et paramètres de démonstration.
4. Tester avec des données de test.
5. Valider avant production : Il faut preciser la methode de recherche du noeud Google Sheets pour identifier exactement la ligne du jour. Il manque une gestion d erreur si l API LinkedIn echoue avant de marquer le post comme publie. Les tags doivent etre en anglais pour correspondre aux categories natives de n8n..

## Limites connues

ne gere pas les images ni les videos dans les posts, uniquement du texte brut. Ne publie pas sur les pages entreprise LinkedIn, seulement sur les profils personnels. Ne cree pas le contenu, il doit etre redige a l avance dans le Google Sheets. Ne gere pas les fuseaux horaires autres que celui du serveur n8n.

