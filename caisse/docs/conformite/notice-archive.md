# Notice — archive fiscale Kebaba Caisse

## Objet

Cette notice décrit le format d’**archive technique** destinée à un contrôle de l’administration fiscale. Elle est distincte d’une sauvegarde de reprise après panne.

## Format

- Type : JSON UTF-8 (`kebaba-fiscal-archive-v1`)
- Contenu : période (`periodFrom` / `periodTo`), horodatage de génération, détail des tickets de **production** (hors FACTICE), empreintes d’intégrité (`integrityHash`, `previousIntegrityHash`), `archiveHash` SHA-256 du corps
- Notice : ce document + copie courte `NOTICE_ARCHIVE.txt` à côté des fichiers

## Emplacement typique appareil

`filesDir/archives/fiscal/<periode>_<id>.json`

## Conservation

Durée de référence : **6 ans** (LPF L. 102 B). Conserver archives + preuves d’intégrité.

## Purge

Aucune purge de détail sans archive complète couvrant la période. Les totaux perpétuels restent en ligne.

## Lecture

Tout éditeur JSON standard. Pas de format propriétaire fermé.
