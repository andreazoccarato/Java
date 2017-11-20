CHAT TCP

Il progetto è composto da tre file principali:
-Client.java
-Server.java
-SQLHelper.java

Per la realizzazione del progetto vengono utilizzati degli stream socket basati sul protocollo di livello trasporto TCP che garantiscono
una communicazione affidabile, fullduplex e orientata alla communicazione.

SERVER
Il Server gestisce le varie connessioni tra i diversi client. Per ogni client che invia una richiesta di connessione, il Server lancia un Thread che 
gestisce il client, e lo salvera in un'apposita struttura dati contenente anche le informazioni della disponibiltà .
Per gestire in modo ottimale i Thread vengono utilizzati i thread pool e gli executor. Ogni Thread all’interno del pool grazie all'executor (classe factory)
può essere riutilizzati.

Importante all'interno del server è la classe Runnable ClientThread utilizzata per la gestione dei client. Che effettua il login o il logon sul DB e gestisce 
la richiesta di connessione con un client specificato e fa partire due thread che permettono di effettuare la communicazione tra i client tramite l'utilizzo 
degli stream.

È stato riscontrato il problema ricorrente in telecomunicazioni secondo cui i due client per communicare devono sincronizzare perfettamente le loro azioni per inviare
dei messaggi (richiesta di communicazione con l'altro client) che però possono andare persi o non ricevuti perchè è in corso un altra azione.
Per risolvere il problema è stato attivato un timer allo scadere del quale se il client non ha preso una decisione sarà disconnesso.

CLIENT
La classe client invia una richiesta di connessione verso il server che se viene accettata procede con l'autenticazione o registrazione 
sul DB e alla connessione con un client desiderato per inizare la communicazione. Dopo di che se tutto è andato a buon fine viene lanciato un 
Thread che continuamente ascolterà il server e mostrerà i messaggi ricevuti.

SQLHelper
La classe SQLHelper permette al server di interfacciarsi con un Database creato in SQLlite. Più precisamente
permette di eseguire le richieste di inserimento (Tramite INSERT) e controllo (tramite SELECT) da parte del Server.