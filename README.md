# Java-cafe
A variety of bioinformatics programs written in Java.

* To download the compiled runnable jars see [releases](https://github.com/dariober/Java-cafe/releases).
* For documentation see [wiki pages](https://github.com/dariober/Java-cafe/wiki) relative to each program and/or execute each program as:
```
java -jar <thiprogram-x.y.z>.jar --help
```
```shell
wget https://github.com/dariober/Java-cafe/releases/download/v0.1.0/MarkovSequenceGenerator-0.1.0.jar

java -jar MarkovSequenceGenerator-0.1.0.jar -h

usage: MarkovChainGenerator [-h] -f FASTA [-nr NRANDOM] [-l LENGTH] [-o OUTPUT] [-n NORDER]
                            [-p PCT_PSEUDOCOUNTS] [--version]

DESCRIPTION
Generate random sequences from input sequence using Markov chain model

optional arguments:
  -h, --help             show this help message and exit
  -f FASTA, --fasta FASTA
                         Input fasta file to generate sequences from. Can be gzipped.
  -nr NRANDOM, --nrandom NRANDOM
                         Number of chains to generate. *Each* sequence in fastainput will be randomized this many times (default: 1)
  -l LENGTH, --length LENGTH
                         Lenght of each random sequence. Default to length of input sequence (default: -1)
  -o OUTPUT, --output OUTPUT
                         Output file. Use - to stdout (default: -)
  -n NORDER, --norder NORDER
                         Order of the Markov model. Must be greater than 0. (default: 1)
  -p PCT_PSEUDOCOUNTS, --pct_pseudocounts PCT_PSEUDOCOUNTS
                         If the input sequence does not contain all the possible  kmers,add  the  missing  ones  at  this percentage of the total counts. E.g. -p 0.1
                         makes all the missing kmers to sum up to no more  than  10%  of  the  total  actual counts. Setting to zero might terminatethe chain with an
                         error. (default: 0.1)
  ```
