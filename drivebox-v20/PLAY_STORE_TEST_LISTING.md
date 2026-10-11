# DriveBox AI — Google Play Test Listing

## App identity
- App name: DriveBox AI
- Package: lu.guerrida.drivebox
- Version name: 2.0.0-preview
- Version code: 20
- Category: Productivity
- Audience: General / Family use

## French listing
### Short description
Transformez votre Android Box en cloud familial privé, simple et intelligent.

### Full description
DriveBox AI transforme une Android Box ou un receiver compatible en cloud familial privé accessible depuis votre réseau local.

Chaque membre dispose de son espace personnel protégé par mot de passe pour stocker, organiser et consulter ses fichiers.

Fonctions principales :
- profils familiaux séparés ;
- stockage sur USB, SSD ou stockage compatible ;
- import, création de dossiers, renommage, suppression et téléchargement ;
- aperçu des photos, vidéos, musique et PDF ;
- tableau de bord avec statistiques ;
- recherche intelligente par type de fichier ;
- détection simple de doublons probables ;
- analyse des fichiers volumineux et récents ;
- assistant DriveBox AI local pour retrouver rapidement photos, vidéos, PDF, ZIP et gros fichiers.

La version Preview fonctionne principalement sur le réseau local. Les fonctions d’assistance intelligente incluses dans cette version n’envoient pas vos fichiers vers un service d’IA externe.

DriveBox AI est conçu pour les utilisateurs qui souhaitent conserver le contrôle de leur stockage tout en bénéficiant d’une interface simple et moderne.

### Release notes — 2.0 Preview
Première Preview Google Play de DriveBox AI : nouveau design, tableau de bord intelligent, profils famille, gestionnaire de fichiers, analyse du stockage et assistant local.

## English listing
### Short description
Turn your Android Box into a private, simple and intelligent family cloud.

### Full description
DriveBox AI turns a compatible Android Box or receiver into a private family cloud available on your local network.

Each family member gets a password-protected personal space to store, organize and access files.

Main features:
- separate family profiles;
- USB, SSD or compatible storage support;
- upload, folders, rename, delete and download;
- photo, video, audio and PDF preview;
- smart dashboard and storage insights;
- file-type search;
- basic duplicate detection;
- large and recent file analysis;
- local DriveBox AI assistant for finding photos, videos, PDFs, ZIP archives and large files.

This Preview mainly works on your local network. The included smart assistant features do not send your files to an external AI service.

### Release notes — 2.0 Preview
First Google Play Preview of DriveBox AI with a redesigned interface, family profiles, file manager, storage analysis and local smart assistant.

## Internal testing instructions
1. Install DriveBox AI on the Android receiver/box.
2. Open DriveBox AI and select a storage location or USB/SSD.
3. Start the local server.
4. Scan the QR code or open the displayed local URL from another device on the same Wi‑Fi network.
5. Choose or create a family profile and set a password.
6. Test folder creation, upload, preview, rename, delete, download and DriveBox Intelligence.

## Data safety draft
- Account creation: local family profiles only.
- Personal data sharing with third parties: none in this Preview.
- File transfer to external AI providers: none in this Preview.
- Primary operation: local network and user-selected storage.
- Passwords: stored as derived password hashes, not plain-text passwords.

IMPORTANT: Review the final Google Play Data safety form against the exact production build before public release.

## Sensitive permission note
DriveBox currently requests broad storage access because its core function is file management on user-selected local/USB storage. Google Play may require a declaration/review for MANAGE_EXTERNAL_STORAGE. For public production, keep this permission only if Play policy eligibility is confirmed; otherwise prefer Storage Access Framework/app-scoped storage.

## Suggested test track name
DriveBox AI — V2 Preview Internal

## Suggested tester feedback questions
- Did storage selection work on your device?
- Could you create a folder and upload a file?
- Did previews work for images/video/PDF?
- Was the family profile flow clear?
- Did the DriveBox Intelligence commands return useful results?
- Did you encounter any receiver/Android TV navigation issue?
