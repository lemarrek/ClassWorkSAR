## Architecture : Exclusion Mutuelle Distribuée (DME) Tolérante aux Pannes

Ce document détaille la conception d'un service d'exclusion mutuelle centralisé (un serveur, $N$ clients), conçu pour résister aux défaillances des nœuds sans perte de requêtes ni blocage définitif (deadlock). L'architecture se décline en deux stratégies de persistance : **Stateless** (sans état) et **Stateful** (avec état).

---

## 1. Modèle de Système et Hypothèses

L'architecture repose sur les postulats réseau et matériels suivants :
* **Asymétrie de découverte :** Le client connaît l'adresse du serveur. Le serveur ignore l'adresse des clients. En cas de coupure, l'initiative de la reconnexion appartient toujours au client.
* **Canaux de communication :** Les connexions garantissent l'ordre et la livraison des messages sans perte. Lors d'une panne serveur, les messages en transit sont perdus.
* **Détecteur de fautes parfait :** Le système s'appuie sur un oracle infaillible. Il notifie instantanément et sans faux positifs la chute ou le retour d'un processus (`processDown`, `processUp`). 
* **Modèle de crash serveur :** Le serveur suit un modèle *Crash-Recovery*. Lors d'une panne, la mémoire vive (RAM) est perdue, mais le stockage persistant (disque) reste intact.
* **Topologie statique :** Le serveur charge la liste exhaustive des clients autorisés au démarrage via un fichier de configuration (indépendant de l'état du mutex).

---

## 2. Protocole Nominal

Le système est divisé en deux composants : le **Serveur Central** et la **Bibliothèque Cliente** (fournissant les primitives `lock()` et `unlock()`).

### Les Messages
| Message | Sens | Déclencheur |
| :--- | :--- | :--- |
| `REQUEST` | Client $\rightarrow$ Serveur | Appel de `lock()` par l'application. |
| `GRANT` | Serveur $\rightarrow$ Client | Le serveur attribue le mutex. |
| `RELEASE` | Client $\rightarrow$ Serveur | Appel de `unlock()` par l'application. |
| `HELLO(id, state)` | Client $\rightarrow$ Serveur | Reconnexion suite à une panne serveur. |

### Automate du Serveur
Le serveur maintient deux variables en mémoire :
* `owner` : L'identifiant du client détenant le mutex (ou null).
* `waiting` : Une file d'attente FIFO des clients en attente.

**Comportement :**
* Sur `REQUEST` : Si `owner` est libre, le client devient `owner` et reçoit `GRANT`. Sinon, il est poussé dans `waiting`.
* Sur `RELEASE` : Le premier client de `waiting` (s'il existe) devient `owner` et reçoit `GRANT`. Sinon, `owner` devient libre.

### Automate de la Bibliothèque Cliente
La bibliothèque gère l'état logique du client de manière asynchrone par rapport au réseau.
* **`IDLE`** : Aucune ressource requise.
* **`WAITING`** : Appel de `lock()` effectué, attente du `GRANT`.
* **`HOLDING`** : Mutex acquis (`GRANT` reçu), section critique en cours.

> **Principe fondamental :** Les appels `lock()` et `unlock()` modifient l'état local du client **immédiatement**, que le serveur soit joignable ou non. C'est ce découplage qui garantit qu'aucune intention de l'application n'est perdue pendant une coupure réseau.

---

## 3. Détection et Gestion des Pannes

La résilience repose sur une surveillance mutuelle entre le serveur et les clients.

### Panne d'un Client
**Détection :** Le détecteur côté serveur lève `clientFailed(id)`.
**Action :** Le serveur empêche la famine. Si le client mort était `owner`, le mutex est révoqué et attribué au premier de la file `waiting`. S'il était dans `waiting`, il en est simplement retiré.

### Panne du Serveur
**Détection :** Le détecteur côté client lève `serverDown()`. Le serveur ne pouvant prévenir de sa chute, c'est au client de réagir.
**Action locale (Client) :** L'envoi de messages est suspendu. L'application appelant `unlock()` passe l'état en `IDLE` et retourne un succès immédiat. L'appel à `lock()` passe l'état en `WAITING` et bloque le thread. Un client en `HOLDING` continue l'exécution de sa section critique sans interruption.
**Reconnexion :** Au signal `serverUp()`, le client ouvre la connexion et envoie `HELLO(id, état_courant)`. **Aucun historique de requêtes n'est rejoué**, seul l'état absolu actuel fait foi, rendant l'opération idempotente.

---

## 4. Stratégies de Recouvrement (Crash Serveur)

### Option A : Serveur Stateless (Sans état)
Le serveur ne sauvegarde rien sur disque. Au redémarrage, `owner` et `waiting` sont vides. Le serveur doit reconstruire son état en interrogeant implicitement les clients via leurs messages `HELLO`.

* **Phase de récupération (Recovery) :** Le serveur gèle toute attribution de mutex. Il attend de recevoir soit un `HELLO(id, state)`, soit un `clientFailed(id)` pour **chaque** client de sa configuration statique.
* **Reconstruction :**
  * `HELLO(id, HOLDING)` $\rightarrow$ Le client est restauré comme `owner`.
  * `HELLO(id, WAITING)` $\rightarrow$ Le client est poussé dans `waiting`.
  * `HELLO(id, IDLE)` $\rightarrow$ Ignoré.
* **Fin de phase :** Une fois tous les clients comptabilisés, le serveur reprend son cycle normal.
* **Limites :** L'ordre FIFO de la file d'attente est perdu ; le nouvel ordre dépend de la vitesse de reconnexion des clients. L'équité stricte n'est plus garantie, bien qu'aucune demande ne soit perdue.

### Option B : Serveur Stateful (Avec persistance)
Le serveur écrit chaque modification de `owner` et `waiting` sur le disque **avant** d'envoyer un message (`Write-Ahead Logging`), garantissant que le disque n'est jamais en retard sur ce que croient les clients.

* **Reboot :** Le serveur recharge `owner` et `waiting` depuis le disque. L'ordre FIFO est conservé.
* **Si le disque indique un `owner` (ex: Client A) :** Le serveur doit attendre de connaître l'état de A (via `HELLO` ou `clientFailed`) avant toute action.
  * *A répond `HOLDING`* : Parfaite synchronisation, reprise normale.
  * *A répond `WAITING`* : Le crash serveur a eu lieu après l'écriture disque mais avant l'envoi du `GRANT`. Le serveur lui renvoie un `GRANT`.
  * *A répond `IDLE` ou `clientFailed(A)`* : A a rendu le mutex (ou est mort) pendant le crash serveur. Le serveur libère le mutex et l'attribue au suivant.
* **Traitement des autres clients :** Si un client se déclare `WAITING` mais n'est pas sur le disque, il est ajouté en fin de file. S'il se déclare `IDLE` mais est dans la file disque, il est retiré.
* **Limites :** Les écritures synchrones sur disque à chaque changement de possesseur impactent les performances en temps normal. Une perte d'ordre mineure subsiste si un client fait un cycle complet `unlock()` puis `lock()` pendant la panne : il déclarera `WAITING` à la reconnexion et se verra réattribuer le mutex s'il était l'`owner` sauvegardé, court-circuitant la file.

---

## 5. Preuve de Zéro Perte de Requête

La conception garantit la conservation des requêtes via la réconciliation d'état au retour du serveur, indépendamment du moment du crash :

1. **`REQUEST` perdu en transit :** Le client est passé en `WAITING` localement. À la reconnexion, il envoie `HELLO(WAITING)`, forçant le serveur à l'insérer dans la file.
2. **Crash entre la réception du `REQUEST` et l'envoi du `GRANT` :** Côté client, l'état est toujours `WAITING`. Identique au cas précédent.
3. **`RELEASE` perdu en transit :** L'état local du client est `IDLE`.
   * *Stateless :* Personne ne réclame l'état `HOLDING`, le serveur déduit que le mutex est libre à la fin de la phase de récupération.
   * *Stateful :* Le serveur lit l'ancien `owner` sur le disque, attend son `HELLO(IDLE)`, comprend que le mutex a été libéré, et corrige son état.
4. **Crash pendant la phase de récupération :** Les clients n'ayant pas reçu de `GRANT` restent dans leur état (`WAITING` ou `HOLDING`) et renverront leur `HELLO` lors du redémarrage suivant.
