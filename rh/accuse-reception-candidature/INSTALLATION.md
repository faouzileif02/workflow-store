# Installation — Accusé réception automatique candidature formulaire

## Prérequis

Compte Google (Gmail + Google Sheets) et un formulaire configuré pour envoyer les données vers le webhook n8n.

1. Importer le JSON commercial dans n8n.
2. Configurer Gmail/Google Sheets et le webhook du formulaire.
3. Remplacer les identifiants de démonstration.
4. Tester une candidature complète puis une candidature incomplète.
5. Valider avant production : La structure exacte des colonnes attendues dans Google Sheets doit etre precisee dans la description. Le noeud Respond to Webhook doit etre connecte juste apres le trigger pour eviter un timeout du formulaire distant. La gestion de la piece jointe n est pas robuste, un simple lien limite l accessibilite si le fichier recu est prive..
