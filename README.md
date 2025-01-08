# Application Android de Jugement d'Oeuvres

## Description du projet

Ce projet est une application Android développée dans le cadre de la deuxième année du cursus **Réseaux et Télécommunications**. L'application permet aux utilisateurs de juger et d'évaluer différentes œuvres (films, livres, musiques, etc.). Elle propose une interface simple et intuitive pour permettre aux utilisateurs de partager leurs opinions et d'attribuer des notes aux œuvres qu'ils consultent.

## Fonctionnalités

- Parcourir les œuvres disponibles
- Donner une note et laisser un commentaire
- Consulter les évaluations des autres utilisateurs
- Filtrer les œuvres par catégorie ou tag
- Faire des demandes d'ajout d'oeuvre

## Technologies utilisées

- **Langage de programmation :** Java
- **Plateforme :** Android
- **Base de données :**
- **IDE :** Android Studio

## Contribution

Ce projet a été réalisé par quatre étudiants en 2ème année de Réseaux et Télécommunications :

- **Thomas Deloup (xKayaato)** - [Rôle :]
- **Jérémy Guibert (JeremyGuib)** - [Rôle :]
- **Pierre Theles Fache (Pierre1080)** - [Rôle :]
- **Mathis viger (Mathisvgr)** - [Rôle :]

---

# Installation du serveur et configuration

Ce projet contient des fichiers PHP qui interagissent avec une base de données. Suivez les étapes ci-dessous pour installer et configurer le serveur.

## Prérequis

* Un serveur Linux avec Apache ou Nginx.
* PHP et PDO (pour se connecter à la base de données).
* Une base de données SQLite ou MySQL/MariaDB.
* Accès en ligne de commande (SSH) sur le serveur.

## Étapes d'installation

1. **Placer les fichiers sur le serveur**

   Déplacez les fichiers PHP et la base de données dans le répertoire `/var/www/html`.

   ```bash
   mv /chemin/source/*.php /var/www/html/
   mv /chemin/source/nom_base.db /var/www/html/


2. **Configurer les permissions**

     Assurez-vous que les fichiers et la base de données ont les bonnes permissions :
  
    Appliquer les permissions sur les fichiers PHP :
    ```bash
    chmod 644 /var/www/html/*.php
    ```

    Appliquer les permissions sur la base de données (SQLite) :
    ```bash
    chmod 664 /var/www/html/nom_base.db
    ```
  
    Donner les permissions nécessaires au répertoire /var/www/html :
    ```bash
    chmod 775 /var/www/html
    ```
  
    Vérifiez que le propriétaire et le groupe du dossier sont configurés pour le serveur web :
    ```bash
    chown -R www-data:www-data /var/www/html
    ```

3. **Configurer la connexion à la base de données dans connect.php**
  
    Ouvrez le fichier connect.php et modifiez la ligne qui pointe vers la base de données pour refléter le bon chemin :
    
    ```bash
    nano /var/www/html/connect.php
     ```
    Modifiez cette ligne pour :
    
    ```php
    $pdo = new PDO('sqlite:/var/www/html/nom_base.db');
    ```
    Enregistrez le fichier et quittez l'éditeur (Ctrl + O pour enregistrer, puis Ctrl + X pour quitter).
    
4. **Préparer les données pour Java**
    Créez un fichier CSV (table.csv) pour insérer les données dans la base. Le format attendu est :
    
    ```csv
    nomOeuvre,dateSortie,auteur_studio,actif,type
    Naruto,2002-10-03,Masashi Kishimoto,1,anime
    Titanic,1997-12-19,James Cameron,1,film
    ```
    Enregistrez le fichier CSV et placez-le à un emplacement accessible par votre application Java.

## Sécurité
  Ne placez pas la base de données dans un répertoire accessible via HTTP. Si possible, déplacez-la en dehors de /var/www/html/ et ajustez le chemin dans connect.php. (dans notre cas, on met dans le /var/www/html)

  Si vous devez la laisser dans /var/www/html/, utilisez un fichier .htaccess pour interdire l'accès direct à la base de données (Apache) :

  ```apache
  <Files "nom_base.db">
  Require all denied
  </Files>
  ```

---

© 2024 - Projet étudiant - Réseaux et Télécommunications, 2ème année
