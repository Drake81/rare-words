# Information Retrieval - Praktikum

## Aufgabenstellung

1. Häufigkeit der Wörter ermitteln (über verschiedene Datenbanken)
2. Herausfinden seltener Wörter (durch Festlegung eines Schwellwerts)
3. Gewichtung der Sätze durch Anzahl seltener Wörter in einem Satz
4. Ähnliche Sätze finden anhand Festlegung
  + **Übereinstimmende Wörter**
  + Prozentuale Abhängigkeit
5. Anzeige "erster" (ausgewählter) Ergebnisse 

## Noch umsetzbar

4. Ähnliche Sätze finden anhand Festlegung
  + Nicht übereinstimmende Wörter
  + Datum
  + Prozentuale Abhängigkeit

### Credentials

Kopieren von credentials.properties.example nach credentials.properties.

``` sh
cp credentials.properties.example credentials.properties
$EDITOR credentials.properties
```

### Logging

Kopieren von logs/logging.properties.example nach logs/logging.properties.

``` sh
cp logs/logging.properties.example logs/logging.properties
```

## Tabelle - Anzahl aller Wörter

Table-Name: *word_frequency*

| word (PK) VARCHAR(70) | frequency BIGINT(20) |
| ------------- |:-------------:| 
| blablub | 2000 |
| ... | ... |

Erstellt mit:

```
CREATE TABLE `word_frequency` (
  `word` varchar(70) NOT NULL,
  `frequency` bigint(20) NOT NULL,
  PRIMARY KEY (`word`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```

## Tabelle - Anzahl ähnlicher Sätze

Table-Name: *sentence_similarity*

| s_id_1 (FK) | s_id_2 (FK) | similarity_count float |
| ------------- |:-------------:|:-----:|
| blablub | 2000 | 0.5 | 
| ... | ... | ... |

Erstellt mit:

```
CREATE TABLE `sentence_similarity` (
  `similarity` float NOT NULL,
  `s_id_1` int(10) NOT NULL,
  `s_id_2` int(10) NOT NULL,
  PRIMARY KEY (`s_id_1`,`s_id_2`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
```
