# Sistema di gestione delle richieste di recapito digitale certificate

## Obiettivo

Realizzare un sistema distribuito per la gestione di richieste di invio documenti digitali certificati verso destinatari
identificati, con tracciamento dello stato delle operazioni e meccanismi di notifica interna.

## Requisiti funzionali

Requisiti funzionali
1. Gestione Destinatari
   - Il sistema deve consentire la registrazione e consultazione di destinatari a cui è possibile inviare comunicazioni.
   - Ogni destinatario è associato a un identificativo univoco, un indirizzo digitale e uno stato di validità del recapito.
2. Gestione delle Richieste di Invio
   - Deve essere possibile creare una richiesta di invio digitale specificando documenti, destinatari e tipologia di invio.
   - Ogni richiesta deve essere tracciata e avere uno stato (in attesa, in elaborazione, completata, fallita).
3. Tracciamento e consultazione
   - Consultazione dello stato di una richiesta e dei dettagli destinatari.
   - Disponibilità di informazioni su tempi e stato finale.
4. Notifiche interne
   - Cambio stato di una richiesta genera una notifica verso un sistema interno.
   - Le notifiche devono essere tracciabili o persistite.

## Architettura ad alto livello

Per la realizzazione di questa soluzione ho adottato un'architettura a microservizi basata sull'approccio DDD (Domain Driven Design) permettendo 
una suddivisione delle responsabilità tra ogni dominio basato sul perimetro funzionale e di dati di dominio.

In particolare, sulla base dei requisiti funzionali, ho identificato e implementato i seguenti domini funzionali:

### Bounded-Contexts

- **Recipient Domain**: gestione, validazione e consultazione dei destinatari certificati
- **Request Domain**: entry-point per la creazione e orchestrazione dello stato delle richieste inoltrate
- **Delivery Domain**: worker del processo di delivery, esegue validazioni, comunica con i provider esterni addetti all'invio
- **Tracking Domain**: gestisce l'audit e la timeline degli eventi per ogni richiesta
- **Notification Domain**: gestisce l'invio delle notifiche verso un sistema interno in base ai cambi di stato delle richieste

### Microservizi (domini funzionali)

Tramite l'identificazione dei domini funzionali ho realizzato i seguenti microservizi:

| Microservizio | Responsablità                                                                                                                                    | Tech principali                                                                                    | Persistenza |
|---|--------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------------------------------------|-------------|
| **request-service** | Orchestrazione stato della richiesta<br/>API di creazione/consultazione<br/>Outbox publisher<br/>Consumer eventi delivery                        | Java 21<br/>Spring Boot 3<br/>Spring Cloud Stream (Kafka)<br/>Spring MVC<br/>JPA                   | MySQL 8     |
| **delivery-service** | Esecuzione del processo di delivery: validazioni async (destinatari/documenti<br/>Invio della richiesta tramite provider<br/>Pubblicazione esiti | Java 21<br/>Spring Boot 3<br/>WebFlux (Reactor)<br/>Spring Cloud Stream (Kafka)<br/>Reactive Mongo          | MongoDB 7   |
| **recipient-service** | Gestione e validazione destinatari                                                                                                               | Java 21<br/>Spring Boot 3<br/>Spring MVC<br/>JPA                                                   | MySQL  8    |
| **tracking-service** | Audit e timeline eventi                                                                                                                          | Java 21<br/>Spring Boot 3<br/>WebFlux (Reactor)<br/>Spring Cloud Stream (Kafka)<br/>Reactive Mongo | MongoDB     |
| **notification-service** | Notifica verso servizio interno in base ai cambi di stato                                                                                        | Java 21<br/>Spring Boot 3<br/>WebFlux (Reactor)<br/>Spring Cloud Stream (Kafka)<br/>Reactive Mongo | MongoDB  7  |

## Scelte tecniche adottate per i microservizi

Per la scelte delle tecnologie utilizzate per l'implementazione dei microservizi mi sono basato sul perimetro funzionale, sulla base di un'idea del carico
e dei vincoli (come ad esempio quelli transazionali) o di operazioni che ogni microservizio deve eseguire.

In particolare ho preferito utilizzare un approccio imperativo sui microservizi `recipient-service` e `request-service` in quanto il primo espone REST API
puramente riferite alla gestione CRUD dei destinatari, quindi ho preferito utilizzare un database con struttura relazionale mentre per il secondo dovendo gestire
la persistenza delle richieste e le informazioni accessorie e implementando il pattern di Transactional Outbox, sia per la fase di scrittura, sia per la fase di polling e aggiornamento
ho preferito basarmi su un approccio imperativo su base database relazionale.

Per quanto riguarda i microservizi `delivery-service`, `tracking-service` e `notification-service` ho preferito un approccio reattivo in quanto sono servizi che eseguono processi
asincroni legati al processo di consumo di eventi, di integrazioni con servizi esterni (sfruttando WebClient), dove ho voluto sfruttare i vantaggi del paradigma reattivo. La base di persistenza di questi
microservizi è MongoDB, in quanto oltre alla struttura dei dati più flessibile, si presta alla mole di operazioni I/O previste per un worker come il `delivery-service` e per la gestione delle timeline degli eventi.

### Infrastruttura utilizzata

- **Kafka**: message broker per la comunicazione asincrona tra microservizi
- **MySQL 8.0**: database relazionale per `request-service` e `recipient-service`
- **MongoDB 7.0**: database NoSQL per `delivery-service`, `tracking-service` e `notification-service`

### Principi architetturali adottati

- **DDD (Bounded Context)**: ogni microservizio ha un perimetro funzionale ben definito, utilizza un proprio database per la gestione interna dei dati.
- **Event Driven Architecture**: le propagazione di eventi o comandi di business tra microservizi avviene attraverso topic Kafka.
- **Saga**: il microservizio `request-service` mantiene lo stato della richiesta, produce eventi di cambio stato e comandi di delivery verso `delivery-service` e ne consuma gli esiti.
- **Transactional Outbox**: `request-service` utilizza il pattern outbox per garantire l'affidabilità nella pubblicazione di comandi verso `delivery-service`.
- **Idempotent Consumer**: ogni microservizio interessato dal processo di comunicazione asincrona si basa sul controllo degli eventId/requestId già consumati.

### Database per microservizio

I database utilizzati e la loro struttura viene riepilogata di seguito:

**Recipient Domain**
- database: recipient-service (MySQL)
  - tabella: recipients (salvataggio informazioni destinatari certificati)
  - tabella: recipients_status_audit (salvataggio storico cambio stati destinatari)

**Request Domain**
- database: request-service (MySQL)
  - tabella: requests (salvataggio richieste di invio con informazioni base)
  - tabella: request_recipients (salvataggio destinatari associati a una richiesta)
  - tabella: request_documents (salvataggio documenti associati a una richiesta)
  - tabella: outbox_events (salvataggio eventi outbox per comunicazione asincrona)
  - tabella: consumed_events (salvataggio eventi consumati per idempotenza)

**Delivery Domain**
- database: delivery-service (MongoDB)
  - collection: delivery_attempts (salvataggio tentativi di delivery relativi alla richiesta di avvio processo di consegna emessi dal `request-service`)
  - collection: consumed_events (salvataggio eventi consumati per idempotenza)

**Notification Domain**
- database: notification-service (MongoDB)
  - collection: notification_attempts (salvataggio tentativi di notifica relativi al cambio stato emessi dal `request-service`)
  - collection: consumed_events (salvataggio eventi consumati per idempotenza)

**Tracking Domain**
- database: tracking-service (MongoDB)
  - collection: tracking_events (salvataggio eventi/comandi del `request-service` e `delivery-service` )
  - collection: consumed_events (salvataggio eventi consumati per idempotenza)

## Stati della richiesta di invio gestiti

- `IN_ATTESA` (richiesta acquisita ma non ancora processata - stato attribuito in fase di persistenza della richiesta dal `request-service`)
- `IN_ELABORAZIONE` (processo di validazione e delivery avviata - stato attribuito dal `request-service` al consumo dell'evento d`DeliveryProcessingStartedEvent`)
- `COMPLETATA` / `FALLITA` (processo di delivery concluso con successo o errore - stato attribuito dal `request-service` al consumo dell'evento di `DeliveryCompletedEvent` o `DeliveryFailedEvent`)

## Topic Kafka

| Topic | Tipo di Evento                                                   | Producer | Consumer |
|---|------------------------------------------------------------------|---|---|
| `request.events` | Richiesta Creata / Evento di Cambio Stato                        | request-service | tracking-service, notification-service |
| `delivery.commands` | Comando di avvio processo di validazione e invio della richiesta | request-service | delivery-service |
| `delivery.events` | Evento di esito di validazione e invio della richiesta           | delivery-service | request-service, tracking-service |

## Struttura Eventi

Di seguito riporto la struttura degli eventi e comandi che vengono gestiti durante l'intero workflow di invio della richiesta di recapito digitale certificato:

### Comandi (`delivery.commands`)

- **DeliveryStartProcessingCommand**
    ```json
    {
      "requestId": "uuid",
      "deliveryType": "PEC/EMAIL",
      "recipients": ["string"],
      "documents": ["string"],
      "requestedAt": "date-time"
    }
    ```
### Eventi (`delivery.events`)

- **DeliveryProcessingStartedEvent**
    ```json
    {
      "requestId": "uuid",
      "startedAt": "date-time"
    }
    ```

- **DeliveryCompletedEvent**
  ```json
  {
    "requestId": "uuid",
    "deliveredAt": "date-time",
    "providerMessageId": "string"
  }
  ```

- **DeliveryFailedEvent**
    ```json
    {
      "requestId": "uuid",
      "failedAt": "date-time",
      "failureCode": "string",
      "failureMessage": "string"
    }
    ```

### Eventi (`request.events`)

- **RequestCreated**
    ```json
    {
      "requestId": "uuid",
      "deliveryType": "PEC/EMAIL",
      "recipients": ["string"],
      "documents": ["string"],
      "status": "IN_ATTESA",
      "createdAt": "date-time"
    }
    ```
- **RequestStatusChangedEvent**
    ```json
    {
      "requestId": "uuid",
      "oldStatus": "IN_ATTESA/IN_ELABORAZIONE",
      "newStatus": "IN_ELABORAZIONE/COMPLETATA/FALLITA",
      "reason": "string",
      "errorMessage": "string",
      "occurredAt": "date-time"
    }
    ```
  
Ogni evento e comando contiene informazioni essenziali di identificazione e correlazione negli headers del messaggio Kafka:

- `eventId`: UUID univoco per l'evento/comando
- `eventType`: tipo di evento/comando
- `eventTime`: timestamp di creazione dell'evento/comando
- `traceId`: identificativo di tracing per il monitoraggio distribuito
- `correlationId`: identificativo di correlazione per il tracciamento end-to-end della richiesta


## Workflow end-to-end
### Creazione e invio richiesta di recapito digitale certificato

1. **Client → `recipient-service` (REST API)**: creazione/consultazione destinatari validi

2. **Client → `request-service` (REST API)**: creazione richiesta di invio specificando destinatari, riferimento dei documenti e tipologia di invio

3. `request-service`:
    - effettua validazioni base sull'input (formato destinatari, documenti non vuoti, tipologia invio valida)
    - persiste informazioni relative alla richiesta di invio su tabelle `requests`, `recipients` e `documents`
    - persiste record su `outbox_events` (`RequestCreated` e `DeliveryStartProcessingCommand`)

4. **OutboxPoller** (processo interno `request-service`):
    - legge in modalità batch i record in stato PENDING sulla tabella `outbox_events`
    - effettua lock logico su record recuperati (race condition per concorrenza su scalabilità)
    - pubblica eventi `RequestCreated` e `DeliveryStartProcessingCommand` su topic `delivery.commands`
    - aggiorna i record su tabelle `outbox_events` a SENT / RESCHEDULE / DEAD (in base all'esito dell'invio o al numero di tentativi effettuati per il retry)
   
5. **delivery-service** consuma `DeliveryStartProcessingCommand`:
    - registra `consumed_events` (per verificare idempotenza su eventi già processati)
    - pubblica evento `DeliveryProcessingStartedEvent` su `delivery.events` (consumato dal microservizio `request-service` per aggiornare lo stato della richiesta a `IN_ELABORAZIONE`)
    - valida in parallelo destinatari e documenti (effettua chiamate REST al servizio `recipient-service` per la validazione dei destinatari e ad uno stub per la validazione dei documenti)
    - se le validazioni terminano con esito positivo invoca provider di invio su base `deliveryType`, poi pubblica evento `DeliveryCompletedEvent`
    - se le validazioni terminano con esito negativo o l'invio tramite provider fallisce, pubblica evento `DeliveryFailedEvent`
  
6. **request-service** consuma gli eventi pubblicati sul topic `delivery.events`:
    - aggiorna lo stato della richiesta (`COMPLETATA` / `FALLITA`)
    - pubblica `RequestStatusChangedEvent` su topic `request.events`
  
7. **tracking-service** consuma gli eventi sui topic `request.events` e `delivery.events` e li persiste
   - espone una REST API per la consultazione dello stato e la catena di eventi di una richiesta specifica

8. **notification-service** consuma gli eventi sul topic `request.events` e invia notifica al servizio interno sul cambio stato
    - espone una REST API per la consultazione dello stato delle notifiche relative agli eventi di cambio stato di una richiesta specifica

## Gestione degli errori, retry e DLQ

### Request Service

Nel microservizio `request-service` il rischio principale che mi sono posto è quello che la richiesta viene salvata correttamente nel database ma la pubblicazione su Kafka fallisce. 
Per evitare questa situazione, il servizio usa il pattern **Transactional Outbox** tramite il quale gestisce la persistenza della richiesta e informazioni accessorie oltre all'evento/eventi da pubblicare.

Inoltre, il consumer del topic `delivery.events` è stato implementato con un meccanismo di controllo di idempotenza tramite la tabella `consumed_events` (dello specifico database) permettendo di ignorare i messaggi già lavorati.

### Delivery Service

Nel microservizio `delivery-service` il problema che mi sono posto è quello di rendere il consumer idempotente, quindi essere sicuro che eventuali retry, errori non portassero ad una rilavorazione degli eventi e quindi a validazioni inutili e soprattutto invii duplicati.
Per questo motivo il consumer è stato implementato un meccanismo di controllo di idempotenza tramite la tabella `consumed_events` (dello specifico database) permettendo di ignorare i messaggi già lavorati.

### Tracking Service

Nel microservizio `tracking-service` è importante garantire che tutti gli eventi vengano tracciati correttamente. 
Anche in questo caso il consumer è stato implementato con un meccanismo di idempotenza tramite la tabella `consumed_events` (dello specifico database) per evitare duplicazioni nella registrazione degli eventi e soprattutto
nell'invalidare la cronologia e lo stato degli eventi (sfruttando il driver reattivo di MongoDB).

### DLQ

La configurazione di ogni microservizio che consuma messaggi dai topic Kafka prevede una DLQ per gestire i messaggi che non possono essere elaborati per problemi applicativi, di formato o di momentanea indisponibilità dei servizi esterni.

### Retry dei Consumer

La configurazione di Spring Cloud Stream adottata per i microservizi che consumano messaggi dai topic Kafka prevede un meccanismo di retry con backoff 
esponenziale per gestire errori temporanei o momentanee indisponibilità dei servizi esterni (sfruttando la successiva persistenza del messaggio in DLQ in caso di superamento del numero massimo di tentativi).

## Semplificazioni e assunzioni

Elenco di seguito le semplificazioni e alcune assunzioni che ho adottato per la realizzazione di questa soluzione:

- L'upload dei documenti non è gestita realmente, inizialmente avevo pensato allo sviluppo di un microservizio dedicato alla gestione dell'upload di documenti, 
immaginando un servizio realizzato con approccio reattivo, soprattutto per l'integrazione di download dal `delivery-service` (sfruttando il Flux streaming) e gestendo
la persistenza dei documenti su MongoDB (driver reattivo) sfruttano GridFS e la gestione dei file a chunk.
- La validazione dei documenti (`delivery-service`) è delegata ad uno stub che simula l'integrazione con un servizio esterno, in fase di creazione della richiesta viene passato
un riferimento al documento (che potrebbe essere una url con il path del file su un cloud storage piuttosto che una url ad un servizio di download come ipotizzato al punto sopra).
- L'effettivo invio dei documenti ai destinatari certificati è simulato tramite uno stub, ho implementato però un approccio simile a quello del design pattern `Hexagonal` sfruttando
degli adapter che potranno essere sostituiti con implementazioni di provider o invii reali.
- Non sono stati implementati meccanismi più completi di resilienza come circuit breaker ma solo definita gestione di retry con backoff sui consumer Kafka.
- In fase di sviluppo e testing, l'infrastruttura utilizzata gestita tramite Docker preve la creazione di tre partizioni per topic, quindi la concurrency dei consumer 
definita nella configurazione di Spring Cloud Stream di ogni microservizio è stata impostata a 3 per sfruttare il parallelismo.
- Non è stata implementata alcuna autenticazione/autorizzazione sulle REST API esposte dai microservizi.

## Future evoluzioni

Di seguito elenco alcune possibili evoluzioni future che potrebbero essere implementate per migliorare la soluzione:

- Sviluppo di un microservizio dedicato alla gestione del routing tramite Spring Cloud Gateway centralizzando le chiamate REST API verso i microservizi
- Sviluppo test automatizzati per microservizio e test di integrazione end-to-end (al momento sono definite solo le dipendenze Maven per JUnit, Mockito e Testcontainers)
- Implementazione dell'invio delle metriche e logging a sistemi di monitoraggio esterni (Prometheus con Grafana oppure stack ELK)

## Come buildare, eseguire e testare la soluzione

Per rendere la fase di build, esecuzione e testing manuale della soluzione più semplice e veloce, ho utilizzato Docker e Docker Compose 
per la gestione dell'infrastruttura e della build/esecuzione dei microservizi.

Come prerequisito è necessario avere installato:

- `Docker` (variante Docker Desktop per Windows/MacOS oppure Docker Engine per Linux)
- `Docker Compose` (non necessario se si utilizza Docker Desktop)

per eseguire la compilazione ed l'avvio dei microservizi in locale (qualora si volesse) è necessario aver installato:

- `Maven 3.8+`
- `Java 21+`

una volta essersi assicurati di avere i prerequisiti sopra elencati, è possibile eseguire la soluzione seguendo i passaggi di seguito:

1. posizionarsi nella root del progetto `digital-cert-docs-delivery'
2. aprire un terminale all'interno della root del progetto e buildare infrastruttura e microservizi con il comando:
   ```bash
   docker compose -f docker-compose-infra.yml -f docker-compose-apps.yml up -d --build
   ```
   questo comando permette di eseguire i due docker-compose files presenti nella root del progetto; il primo (`docker-compose-infra.yml`) 
   si occupa di avviare l'infrastruttura (Zookeper, Kafka, MySQL, MongoDB) per Kafka il container di init crea i topic necessari,
   per i database gli script di init presenti nel folder ./database/mongo/init/ e ./database/mysql/init/ (eseguiti all'avvio dei container) definiscono
   i database, le tabelle e le collection
3. Una volta avviati i container sarà possibile raggiungere le REST API esposte dai microservizi sui seguenti endpoint:
   - `recipient-service`: http://localhost:8081/swagger-ui.html
   - `request-service`: http://localhost:8082/swagger-ui.html
   - `delivery-service`: non espone REST API (solo modulo worker)
   - `tracking-service`: http://localhost:8084/swagger-ui.html
   - `notification-service`: http://localhost:8085/swagger-ui.html
4. Si consiglia di effettuare i seguenti step per testare manualmente la soluzione:
   - creare uno o più destinatari certificati tramite l'endpoint `POST /api/recipients` del microservizio `recipient-service`
   - creare una richiesta di invio tramite l'endpoint `POST /api/requests` del microservizio `request-service`, specificando gli indirizzi dei destinatari creati,
   i riferimenti ai documenti (stringhe di esempio) e la tipologia di invio (PEC/EMAIL)
   - consultare lo stato della richiesta e la timeline degli eventi tramite l'endpoint `GET /api/requests/{requestId}/tracking` del microservizio `tracking-service` (il requestId è fornito come payload di risposta alla creazione della richiesta)
   - consultare lo stato delle notifiche inviate tramite l'endpoint `GET /api/notifications/requests/{requestId}` del microservizio `notification-service`
5. Una volta terminati i test per rimuovere l'infrastruttura e i microservizi creati è possibile eseguire il comando:
   ```bash
   docker compose -f docker-compose-infra.yml -f docker-compose-apps.yml down -v
   ```
   questo comando rimuoverà tutti i container, le reti e i volumi creati (al momento non sono volutamente persistenti) per l'esecuzione della soluzione.