# ps-eda-kafka-docker-avro-app
## Content
This repository contains example source code from the PluralSight course:

### [Designing Event-driven Applications Using Apache Kafka Ecosystem](https://app.pluralsight.com/library/courses/designing-event-driven-applications-apache-kafka-ecosystem/table-of-contents)

### by Bogdan Sucaciu

### Code example from Module 4: [Communicating Messages Structure with AVRO and Schema Registry](https://app.pluralsight.com/course-player?clipId=a633e779-2aad-4a24-978c-fe508e46f361#:~:text=Communicating%20Messages%20Structure%20with%20AVRO%20and%20Schema%20Registry)
I enjoyed this course very much and I would recommend it to anyone who needs an introduction in event-driven design and would like
to use Apache Kafka software to implement it.

## Purpose
In the course the example was presented running with locally installed kafka, zookeeper & the Confluent schema-registry software components.
This requires some work, which may be instructive if you want to learn about some basic principles of Kafka and how to configure it.
However, it is much more convenient to run all necessary software components in separate docker containers. Especially when you are 
already familiar with Docker and Docker Compose technology.

### The software components
The example code, which is basically a producer writing to and a consumer reading from a single Kafka topic using AVRO
schema's for both the key and value part of the messages being written to the topic. This example code is still build
and run as small Java applications executing their respective main methods on your local computer. Presumably with help from your favorite IDE. (So this part isn´t deployed to docker containers yet).

The Kafka cluster the example code communicates with, however, is entirely deployed as docker containers:
- one container with a single Apache kafka broker, listening on port 2181, 
- one container with a single Zookeeper instance, listening on port 9092
- one container with the Confluent schema-registry server, listening on port 8081.

### Making good use of the Confluent Platform Community Edition components
To get this set up to work quickly, I created a [docker/docker-compose.yml](docker/docker-compose.yml) file based on the
one found in
[GitHub repo: confluentinc cp-all-in-one-community 7.2.1-post](https://github.com/confluentinc/cp-all-in-one/tree/7.2.1-post/cp-all-in-one-community).
- 7.2.1-post is the current default branch reflecting the latest versions of the Apache Kafka & Confluent technology stack at the date of writing (August 2022).
- cp-all-in-one-community refers to all components of Confluent platform technology stack that fall under the 
  [confluent-community-license](https://www.confluent.io/confluent-community-license/). All source code under this licence
  may be accessed, modified and redistributed freely except for creating a SaaS that tries to compete with Confluent.
- To run the original cp-all-in-one-community docker compose offering and explore their code examples see:
	- [cp-all-in-one-community documentation](https://docs.confluent.io/platform/current/tutorials/build-your-own-demos.html?utm_source=github&utm_medium=demo&utm_campaign=ch.examples_type.community_content.cp-all-in-one#cp-all-in-one-community) 
	- [CE Docker Quickstart documentation](https://docs.confluent.io/platform/current/quickstart/ce-docker-quickstart.html)
	- [Further code examples in various languages](https://docs.confluent.io/platform/current/tutorials/examples/clients/docs/clients-all-examples.html#clients-all-examples)

From this all-in-one [`docker-compose.yml`](https://github.com/confluentinc/cp-all-in-one/blob/7.2.1-post/cp-all-in-one-community/docker-compose.yml), which defines all the components that are a part of the Confluent platform community edition,
we only took the three services that are needed to make the example code work and copied them in our own Docker Compose yaml file.
So, under the hood, we are using Docker images, made available by Confluent (for which we are grateful).

## Changes made to the original example source code.

### Introduction of the [user-tracking-interface](user-tracking-interface/pom.xml) maven module
The most important change I made was to create a separate user-tracking-interface maven module. This module contains 
Java classes that are generated from two AVRO schema's, [product_schema.avsc](user-tracking-interface/src/main/resources/avro/product_schema.avsc)
and [user_schema.avsc](user-tracking-interface/src/main/resources/avro/user_schema.avsc) using the 
`org.apache.avro:avro-maven-plugin`. 

These Java classes are part of the messages put together and send to the topic by the producer and read from the topic 
by the consumer. So both the user-tracking-consumer and the user-tracking-producer module depend on the user-tracking-interface.

In the original example the Java classes were generated manually on the command line and copied as model packages in 
both the producer and the consumer maven module. 

My introduction of the separate user-tracking-interface maven module 
- makes running the example less complex as the generated Java classes are created automatically as part of a maven build of the project.
- reduces code duplication.
I think both are great benefits that didn´t take much effort to accomplish.

---
**Note**

For IntelliJ to notice the content of [user-tracking-interface/src/main/generated](user-tracking-interface/src/main/generated)
you need to mark the directory as *Generated Sources Root* by right-clicking on it in the Project view window and
choosing *Mark Directory as* > *Generated Sources Root* from the context menu.

---

### Updating all maven dependencies
I made an effort to update all maven dependencies to the versions available now (August 2022).

## Usage
- Open a terminal in the project/repository root dir
    ```bash
    $ cd docker
    $ docker compose up -d
    $ docker compose ps
    ```
- When the last command shows you that all three services are up and running, you can proceed to create the 
  `user-tracking-avro` topic in the same terminal with
   ```bash
   $ ./create-topic.sh
   ```
- Build the example code with maven (from the project/repository root dir)
  ```bash
  $ mvn clean compile -e
  ```
- On the command line or within your IDE
    - Run the Main class of the user-tracker-consumer module [com.pluralsight.kafka.consumer.Main](user-tracking-consumer/src/main/java/com/pluralsight/kafka/consumer/Main.java).
        - This application will keep running until you stop its process with Ctrl+C
    - Run the Main class of the user-tracker-producer module [com.pluralsight.kafka.producer.Main](user-tracking-producer/src/main/java/com/pluralsight/kafka/producer/Main.java).
        - This application will exit after publishing ten events on the `user-tracking-avro` topic, but you may run it multiple
          times to see multiples of ten events being processed by the consumer. 

## The Schema registration process
In a production environment the schema registry is configured to only accept schemas registered manually by an authorized administrator.
In our test setup, however, any Kafka client reading or writing to a topic is able to register an AVRO schema in the schema registry.

In our user-tracking-interface module we generated Java source code for two schemas with the maven build:
- [`user_schema.avsc`](user-tracking-interface/src/main/resources/avro/user_schema.avsc) for the message key,
- [`product_schema.avsc`](user-tracking-interface/src/main/resources/avro/product_schema.avsc) for the message value.

In the two root classes derived from both schema's [`com.pluralsight.kafka.model.User`](user-tracking-interface/src/main/generated/com/pluralsight/kafka/model/User.java)
and [`com.pluralsight.kafka.model.Product`](user-tracking-interface/src/main/generated/com/pluralsight/kafka/model/Product.java) when
doing a `mvn clean compile` there is a class variable `com.pluralsight.kafka.model.Product.SCHEMA$` of type
`org.apache.avro.Schema`. Therefore, each message send to the Kafka topic contains the complete schema info of both its key
and its value.

When the producer is started it begins creating messages, which are then serialized into an AVRO encoded stream of bytes,
which will be sent to the topic. there it will be checked if the schemas are present in the registry. If not the schemas are
posted to the registry. Then the Consumer asks the registry for both schema's with a GET request. To be able to deserialize
both the keys and values from the messages read from the topic.

All schemas are stored in the registry linked to a subject. The default subject naming strategy is `${topic-name}-key` &
`${topic-name}-value`. So in this particular example we should have two schemas: one linked to the subject 
`user-tracking-avro-key` and one linked to `user-tracking-avro-value`

The schema registry keeps the schemas in memory, but also saves them to a Kafka topic named `_schemas`. We can check all
topics present with the following command:
```bash
$ docker exec broker kafka-topics --bootstrap-server broker:9092 --list
__consumer_offsets
_schemas
user-tracking-avro
```
This shows 3 topics after starting everything with `docker compose up -d` and creating our own schema with 
`./create-topic.sh`. In the `__consumer_offsets` consumers and consumer-groups can maintain the offset data of the last
message that was read successfully from the `user-tracking-avro` topic.

Which subjects and schemas were stored into the registry can be checked with the respective API calls:
- `http://localhost:8081/subjects/`,
- `http://localhost:8081/schemas/`.
From the logging of the schema registry container this can be obtained as well. We can rerun the producer for another ten messages, 
and we see it posting the key and value schemas to the registry again. This is probably not necessary as they are already present.
As the consumer was still running it continues reading the new messages from the topic. It doesn´t need to ask the registry
again for the schemas. When you restart the consumer it will ask for the schemas again as soon as there are new messages
written to the topic by a new producer execution session.
