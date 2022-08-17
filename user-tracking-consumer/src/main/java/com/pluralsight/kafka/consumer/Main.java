package com.pluralsight.kafka.consumer;


import com.pluralsight.kafka.model.Product;
import com.pluralsight.kafka.model.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import java.time.Duration;
import java.util.Properties;

import static java.util.Arrays.asList;

@Slf4j
public class Main {
    private static final String TOPIC = "user-tracking-avro";
    public static void main(String[] args) {

        SuggestionEngine suggestionEngine = new SuggestionEngine();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "user-tracking-consumer");

        props.put("auto.offset.reset","earliest");// so will read from the start of the topic every time
        //props.put("auto.offset.reset","latest");// default, we will read only the last records added

//      The key and value deserializer from the earlier version of the example
//      props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//      props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("key.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("value.deserializer", "io.confluent.kafka.serializers.KafkaAvroDeserializer");
        props.put("specific.avro.reader", "true");
        props.put("schema.registry.url", "http://localhost:8081");


        try(KafkaConsumer<User, Product> consumer = new KafkaConsumer<>(props)) {
            consumer.subscribe(asList(TOPIC));

            while (true) {
                ConsumerRecords<User, Product> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<User, Product> record : records) {
                    suggestionEngine.processSuggestions(record.key(), record.value());
                }
            }
        } catch (Exception e) {
            log.error(String.format("An exception was raised whilst trying to consume from %s", TOPIC), e);
            throw new RuntimeException(e);
        }
    }
}
