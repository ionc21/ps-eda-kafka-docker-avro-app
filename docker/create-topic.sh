#!/usr/bin/env bash

###############################################################################################
# This idempotent script creates the necessary topic when absent.
# It can be run after the command
# docker compose up -d
###############################################################################################
declare USER_TRACKING_TOPIC="user-tracking-avro"
# declare -i USER_TRACKING_TOPIC_ABSENT=1 # 1 meaning truly absent and 0 meaning actually present
declare USER_TRACKING_TOPIC_ABSENT=true
declare -a TOPICS
declare -i INDEX=0

echo "Trying to create the topic $USER_TRACKING_TOPIC if not yet present."

# Checking the presence of USER_TRACKING_TOPIC
for line in $(docker exec broker kafka-topics --bootstrap-server broker:9092 --list); do
   # echo $line
   # [[ "$line" == *"$KIA_TEST_TOPIC"* ]] && echo "Line contains the KIA_TEST_TOPIC"
    if [[ "$line" == "$USER_TRACKING_TOPIC" ]]; then
        USER_TRACKING_TOPIC_ABSENT=false;
    fi
    TOPICS[${INDEX}]=$line;
    (( INDEX++ )) || true;
done

echo "List of all topics already present: ${TOPICS[*]}"
if $USER_TRACKING_TOPIC_ABSENT
then
  echo "The topic ${USER_TRACKING_TOPIC} is still absent."
else
  echo "The topic ${USER_TRACKING_TOPIC} has already been created."
fi
# echo "USER_TRACKING_TOPIC_ABSENT: $USER_TRACKING_TOPIC_ABSENT"

# Creating USER_TRACKING_TOPIC if absent
if [ $USER_TRACKING_TOPIC_ABSENT == true ]; then
    docker exec broker kafka-topics --bootstrap-server broker:9092 --create --topic $USER_TRACKING_TOPIC
fi

echo "current list of topics:"
docker exec broker kafka-topics --bootstrap-server broker:9092 --list
echo "Description of the $USER_TRACKING_TOPIC topic:"
docker exec broker kafka-topics --bootstrap-server broker:9092 --describe --topic $USER_TRACKING_TOPIC