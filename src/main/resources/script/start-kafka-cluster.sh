#!/bin/bash
# kafka cluster bootstrap script
# Notices: when script execute error, please open it whit 'vim -b filename', avoid special characters
# replace special character: sed -i 's/^M//g' start-kafka-cluster.sh

# acquire Old kafka Cluster Pids
pids=`jps | grep Kafka | cut -d ' ' -f 1`
for pid in $pids;
do
  echo "begin stop kafka process, pid ==> ${pid}"
  kill -9 $pid
  if [ $? -ne 0 ]; then
    echo "kafka cluster process stop failed, pid ==> ${pid}"
    exit
  fi
done

echo 'begin to start kafka node'
# kafka Cluster Config Dir
KAFKA_HOME=/data/apps/kafka_2.13-2.5.0
KAFKA_CLUSTER_DIR=/tmp/kafka/cluster
ZOOKEEPER_SERVER=debian:2181,debian:2182:debian:2183

# kafka node numbers
KAFKA_NODE_NUM=3

## kafka bootstrap port
KAFKA_NODE_PORT_PREFIX=909

## use for kafka metric
KAFKA_JMX_PORT_PREFIX=998
SERVER_PROPERTIES=server.properties

# kafka Cluster bootstrap single node
function bootstrapSingleNode() {
    index=$1;
    echo "===> begin to bootstrap $index node"
    mkdir -p "${KAFKA_CLUSTER_DIR}/node${index}/logs"
    KAFKA_NODE_LOG_PATH="${KAFKA_CLUSTER_DIR}/node$index/logs"
    KAFKA_NODE_BASE_PATH="${KAFKA_CLUSTER_DIR}/node${index}"
    echo "KAFKA_NODE_BASE_PATH ==> $KAFKA_NODE_BASE_PATH"
    echo "KAFKA_NODE_LOG_PATH ==> $KAFKA_NODE_LOG_PATH"
    cd $KAFKA_NODE_BASE_PATH
    if [ $? -ne 0 ]; then
      echo "=====> cd dir [ ${KAFKA_NODE_BASE_PATH} ] failed"
    fi

    ## kafka config replace
    cp "${KAFKA_HOME}/config/${SERVER_PROPERTIES}" ./
    echo 'delete.topic.enable=true' >> $SERVER_PROPERTIES
    echo 'auto.create.topics.enable=false' >> $SERVER_PROPERTIES
    echo "port=${KAFKA_NODE_PORT_PREFIX}${index}" >> $SERVER_PROPERTIES
    echo "host.name=`hostname`" >> $SERVER_PROPERTIES

    ## kafka node args replace
    ## broker.id/port/host.name/log.dirs/zookeeper.connect
    sed -i "s|broker.id=.*|broker.id=${index}|g" $SERVER_PROPERTIES
    sed -i "s|log.dirs=.*|log.dirs=${KAFKA_NODE_LOG_PATH}|g" $SERVER_PROPERTIES
    sed -i "s|zookeeper.connect=.*|zookeeper.connect=${ZOOKEEPER_SERVER}|g" $SERVER_PROPERTIES

    echo ${KAFKA_JMX_PORT_PREFIX}${index}
    export JMX_PORT=${KAFKA_JMX_PORT_PREFIX}${index}

    ## bootstrap single node
    echo "command exec args：${KAFKA_NODE_BASE_PATH}/${SERVER_PROPERTIES}, JMX_PORT: ${JMX_PORT}"
    kafka-server-start.sh -daemon "${KAFKA_NODE_BASE_PATH}/${SERVER_PROPERTIES}"
    echo "===> finish bootstrap kafka ${index} node"
}

for ((i=1; i<=$KAFKA_NODE_NUM; i++))
do
	bootstrapSingleNode $i
done

