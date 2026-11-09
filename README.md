# Scenario enumeration per timeout e retry — Eulero originale

Questo progetto estende il tuo `workflow-predictor-starter`, **senza trasformare i retry in XOR**.

## Dove inserire i file

Questo ZIP contiene il progetto completo (basato sullo starter originale): puoi confrontare o copiare i nuovi file `src/main/java/it/workflow/predictor/reliability/`, `evaluation/`, `examples/TwoTaskSequentialDemo.java`, `Main.java`, `src/test/.../RetryModelTest.java`. **Conserva il `pom.xml` che funziona sul tuo computer**, dato che potresti aver cambiato le coordinate Maven della libreria locale.

## Requisiti

- Eulero originale `org.oris-tool:eulero:1.0.0-SNAPSHOT` installato in `.m2` (oppure adatta la dipendenza alla tua versione locale).
- SIRIO `2.0.4-SNAPSHOT` correttamente risolto tramite Eulero.
- JDK 11 o successivo per questo codice (il tuo JDK 27 va bene se Maven/IntelliJ usano la configurazione corretta).

## Avvio in IntelliJ

1. Reload Maven.
2. Esegui `it.workflow.predictor.Main`: **un task**, tre scenari, confronto fra Eq.(4) e Eulero.
3. Esegui `it.workflow.predictor.examples.TwoTaskSequentialDemo`: **due task sequenziali**, sei scenari.
4. Esegui i test JUnit `RetryModelTest` (Maven > Lifecycle > test).

## Matematica implementata

- Eq.(1): `RetryModel.scenarioProbability(task,n)` calcola D_n.
- Eq.(2): `ConditionalDistribution.uniformSuccess` crea per U(a,b) una `Simple` con U(a,min(b,t)), quando p>0.
- Eq.(3): `ScenarioEvaluator.taskInScenario` costruisce **un ritardo deterministico** n*t seguito da una `Simple` condizionata (n<c) oppure originale (n=c).
- Scenario enumeration: `ScenarioEnumerator` genera combinazioni e pesa ogni scenario con il prodotto dei D_n dei task.
- Eulero: `ScenarioEvaluator.evaluate` analizza **ogni workflow di scenario**, usando `DAG.sequence` oppure `DAG.forkJoin`.
- Step 4: `ScenarioMixer.mix` restituisce la CDF totale sommando `peso * CDF_scenario` campione per campione.
- Eq.(4): `RetryModel.taskCdfWithRetries` calcola **indipendentemente** la formula chiusa per un task uniforme e permette il confronto con l'analisi Eulero.

## Limiti intenzionali

1. Sono supportate **solo distribuzioni uniformi**. Per esponenziale, lognormale o generiche, la CDF condizionata dell'Eq.(2) deve essere implementata e convertita in una distribuzione accettata da Eulero/SIRIO. Non basta passare una lambda CDF.
2. La demo gestisce workflow che eseguono tutti i task (sequenza e fork/join). Le **choice originali** richiedono anche probabilita' di selezione delle alternative e un adattamento specifico; sono previste in una fase successiva.
3. I retry seguono il PDF: IID, senza backoff, ultimo tentativo senza timeout.
4. Tutte le analisi Eulero usano la **stessa griglia** (timeBound, timeStep), poi vengono pesate. `ScenarioMixer` verifica la lunghezza, non può dedurre gli istanti dai soli array.
5. Il risultato numerico di Eulero puo' differire dall'Eq.(4) per errore di discretizzazione (come il precedente 0.45 contro 0.50 a step 0.1).
6. Il numero di scenari (prima di escludere quelli a peso zero) e' il prodotto `∏(maxRetries_i+1)`. Aumenta rapidamente.

### Esempio con un task

- T1 ~ U(1,2), timeout=1.5, maxRetries=2.
- Peso n=0: 0.5, durata U(1,1.5).
- Peso n=1: 0.25, durata 1.5 + U(1,1.5).
- Peso n=2: 0.25, durata 3.0 + U(1,2).
- Eq.(4) esatta: F(1.5)=0.5, F(3.0)=0.75, F(4.5)=0.875, F(5.0)=1.
