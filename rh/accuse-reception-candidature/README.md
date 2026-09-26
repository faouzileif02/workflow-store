# Accusé réception automatique candidature formulaire

> **Statut : BETA — validation/corrections en cours**

Les entreprises qui reçoivent des candidatures via un formulaire en ligne ne répondent jamais immédiatement, ce qui donne une image peu professionnelle et laisse les candidats sans nouvelle. Ce workflow envoie automatiquement un accusé de réception personnalisé au candidat dès la soumission, ajoute sa candidature dans un tableau de suivi et notifie le recruteur. Il permet de gagner du temps tout en offrant une meilleure expérience candidat.

## Pour qui ?

Recruteurs, indépendants, RH et petites entreprises qui reçoivent des candidatures via formulaire web.

## Prérequis

Compte Google (Gmail + Google Sheets) et un formulaire configuré pour envoyer les données vers le webhook n8n.

## Version commerciale

Le JSON n8n complet reste privé.

## Point à corriger avant commercialisation

La structure exacte des colonnes attendues dans Google Sheets doit etre precisee dans la description. Le noeud Respond to Webhook doit etre connecte juste apres le trigger pour eviter un timeout du formulaire distant. La gestion de la piece jointe n est pas robuste, un simple lien limite l accessibilite si le fichier recu est prive.

## Limites

Le workflow ne lit pas le contenu du CV joint, ne fait aucun scoring ou classement de la candidature, ne gère pas les relances si le recruteur ne répond pas, et ne s'intègre pas à un ATS existant.

## Tags

candidature, accusé réception, recrutement, formulaire, google sheets
