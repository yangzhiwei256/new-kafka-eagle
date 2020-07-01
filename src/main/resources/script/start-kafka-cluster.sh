#!/bin/bash
# kafka cluster bootstrap script
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
KAFKA_CLUSTER_DIR=$KAFKA_HOME/cluster/tmp
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
    echo "port=909$index" >> $SERVER_PROPERTIES
    echo "host.name=`hostname`" >> $SERVER_PROPERTIES

    ## kafka node args replace
    ## broker.id/port/host.name/log.dirs/zookeeper.connect
    sed -i "s/broker.id=0/broker.id=$index/g" $SERVER_PROPERTIES
    sed -i "s/log.dirs=/$KAFKA_NODE_LOG_PATH/g" $SERVER_PROPERTIES
    export JMX_PORT=998$i

    ## bootstrap single node
    nohup  kafka-server-start.sh "${KAFKA_NODE_BASE_PATH}/${SERVER_PROPERTIES}" > /dev/null &
    echo "===> finish bootstrap kafka ${index} node"
}

KAFKA_NODE_NUM=3
for ((i=1; i<=$KAFKA_NODE_NUM; i++))
do
	bootstrapSingleNode $i
done


# Enable JMX Function For Metrics
#export JMX_PORT=9988
#nohup  kafka-server-start.sh  $KAFKA_CLUSTER_DIR/node1/server.properties > /dev/null &

#export JMX_PORT=9989
#nohup  kafka-server-start.sh  $KAFKA_CLUSTER_DIR/node2/server.properties > /dev/null &

#export JMX_PORT=9990
#nohup  kafka-server-start.sh  $KAFKA_CLUSTER_DIR/node3/server.properties > /dev/null &

