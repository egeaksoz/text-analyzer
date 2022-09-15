# text-analyzer

A project that helps you count the words in a text.

This project has 3 parts. Main text-analyzer, database ( postgres) and a akka-consumer. They have to be started seperately.

## How to run

First start the text-analyzer by 

`sbt run`

Then start the akka-consumer by same command on akka-consumer folder:

`sbt run`

and then start the database, to do that on postgresql folder:

`docker-compose up -d`

This project assumes you have a running instance of kafka ( and zookeeper) on port 9092.

Enjoy!
