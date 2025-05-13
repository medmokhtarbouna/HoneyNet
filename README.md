# HoneyNet - README

SmartHoneyNet est un système de Honeypot simple et interactif, conçu pour simuler des services vulnérables (comme SSH, FTP, HTTP, IRC) afin de détecter, enregistrer et analyser les comportements suspects. Il offre une interface graphique complète, une analyse automatique des journaux, et une cartographie géographique des adresses IP attaquantes.

## Fonctionnalités principales
- Simulation de services réseau vulnérables
- Interface graphique conviviale (Swing)
- Journalisation automatique des tentatives de connexion
- Analyse statistique des journaux
- Visualisation des IPs sur une carte GeoIP (JXMapViewer)
- Export des résultats en PDF et CSV

---

## Structure du projet
```
edu.wustl.honeyrj
├── analysis          → Analyse des fichiers journaux
├── geoip             → Cartographie et géolocalisation IP
├── gui               → Interface graphique Swing
├── honeyrj           → Point d’entrée et classes principales
├── logging           → Gestion des logs
├── lowinteraction    → Moteur de simulation des services
├── protocol          → Protocoles simulés (FTP, SSH...)
└── tools             → Générateurs ou utilitaires
```

---

## Compilation
Assurez-vous que Maven est installé puis exécutez :

```bash
mvn clean install
```

Le fichier `.jar` généré sera disponible dans `target/HoneyRJ-1.0.jar`.

---

## ⚡ Utilisation

### 1. **Lancement de l’interface graphique**
Dans le terminal ou PowerShell :

```bash
java -jar target/HoneyRJ-1.0.jar
```

Cela ouvre l’interface `HoneyRJGUI`.

---

### 2. **Démarrer les modules (FTP, SSH, HTTP, IRC)**
Depuis l’interface graphique :

- Cliquer sur **“Démarrer tout”** ou sur les boutons **“Démarrer”** pour chaque module individuellement.

---

### 3. **Tester les connexions (Windows)**

Ouvrir un **CMD ou PowerShell** et utiliser ces commandes pour simuler des attaques :

#### FTP (port 21)
```bash
ftp localhost
```

#### SSH (port 22)
```bash
ssh test@localhost
```

#### HTTP (port 8080)
```bash
curl http://localhost:8080
```

#### IRC (port 6667)
```bash
telnet localhost 6667
```

> Remarque : Vous pouvez utiliser `telnet` ou `netcat` (`nc`) pour simuler d'autres connexions.

---

### 4. **Analyser les données**
- Cliquer sur **“Afficher les statistiques”** ou **“Analyser toutes les sessions”**.
- Les fichiers `.log` sont automatiquement analysés.

---

### 5. **Afficher la carte GeoIP**
- Cliquer sur **“Carte GeoIP”** pour voir la localisation des IPs sur une carte interactive.

---

### 6. **Exporter les résultats**
- Cliquez sur :
    - **“Exporter en CSV”** pour obtenir un fichier `.csv`
    - **“Exporter en PDF”** pour un rapport structuré

Les fichiers sont enregistrés dans le dossier :
```bash
%USERPROFILE%\HoneyRJExports
```

---

## Dépendances principales
- Java 8+
- Maven
- JXMapViewer2 (pour la carte GeoIP)
- Apache Commons CSV
- iTextPDF

---

## Licence
Ce projet est distribué sous licence MIT.
