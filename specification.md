# Spécification Technique : Channel

## 1. Description Générale
Le composant `Channel` agit comme un tuyau de communication entre les tâches du système. Il assure le transfert d'un flux de données brut (octets) de manière synchrone et sécurisée en environnement concurrent.

## 2. Bidirectionnalité et Flux de Données
Le canal permet de transférer des octets en suivant un modèle producteur-consommateur :
* **Écriture (Write) :** Les données sont poussées depuis un tableau source vers le canal, à partir d'un index (`offset`) sur une longueur définie (`length`). Si le tuyau de destination n'a plus de place (tampon plein), l'opération est bloquante et la tâche est mise en attente. L'opération renvoie le nombre d'octets qui ont pu être insérés.
* **Lecture (Read) :** Les données présentes dans le canal sont extraites et copiées vers un tableau de destination à partir d'un index défini (`offset`), pour une taille maximale demandée (`length`). Si le tuyau est actuellement vide, l'opération devient bloquante et la tâche patiente jusqu'à l'arrivée de nouveaux octets. L'opération renvoie la quantité exacte d'octets récupérés, ou -1 si le canal a été déconnecté.

## 3. Synchronisation et Accès Concurrent
Afin d'éviter la corruption des données, les opérations d'entrée et de sortie sur le flux d'octets respectent des contraintes strictes :
* Un seul flux d'écriture et un seul flux de lecture peuvent manipuler le contenu du tuyau à un instant T (verrouillage mutuel).
* Les changements d'état (passage de "vide" à "contient des données", ou libération d'espace dans le tampon) notifient automatiquement le système pour réveiller les processus endormis.

## 4. Le Phénomène de Déconnexion (Disconnect)
La méthode `disconnect()` déclenche une procédure d'arrêt d'urgence du tuyau. Cet événement provoque une réaction en chaîne immédiate :
* **Fermeture définitive :** L'état du canal bascule. Toute vérification ultérieure via la fonction `disconnected()` affirmera que le canal est hors ligne (retourne `true`).
* **Libération des verrous (Unblocking) :** L'action de déconnexion réveille instantanément l'intégralité des tâches qui étaient bloquées dans une attente infinie (que ce soit sur un `read()` attendant des données, ou un `write()` attendant de la place).
* **Signalisation de fin de flux :** Pour indiquer aux processus réveillés que le tuyau a été coupé (et non pas qu'une opération classique a abouti), les méthodes de lecture interrompues renvoient la valeur d'erreur `-1`. Cela permet aux tâches de s'arrêter proprement sans rester figées indéfiniment.