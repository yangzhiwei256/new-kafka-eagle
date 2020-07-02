#!/bin/bash
# kafka cluster bootstrap script
# Notices: when script execute error, please open it whit 'vim -b filename', avoid special characters
# replace special character: sed -i 's/^M//g' start-zookeeper-cluster.sh

# acquire Old Zookeeper Cluster Pids
pids=`jps | grep QuorumPeerMain | cut -d ' ' -f 1`
for pid in $pids;
do
  echo "begin stop Zookeeper process, pid ==> ${pid}"
  kill -9 $pid
  if [ $? -ne 0 ]; then
    echo "Zookeeper cluster process stop failed, pid ==> ${pid}"
    exit
  fi
done

echo 'begin to start Zookeeper node'
# ZOOKEEPER Cluster Config Dir
ZOOKEEPER_HOME=/data/apps/zookeeper-3.4.14
ZOOKEEPER_CLUSTER_DIR=/tmp/zookeeper/cluster

# ZOOKEEPER node numbers
ZOOKEEPER_NUM=3

## kafka bootstrap port
ZOOKEEPER_NODE_CLIENT_PORT_PREFIX=218
ZOOKEEPER_NODE_SELECTION_PORT_PREFIX=228
ZOOKEEPER_NODE_COMMUNICATION_PORT_PREFIX=238
SERVER_SAMPLE_FILE_NAME=zoo_sample.cfg

# kafka Cluster bootstrap single node
function bootstrapSingleNode() {
    index=$1
    echo "===> begin to bootstrap $index node"
    mkdir -p "${ZOOKEEPER_CLUSTER_DIR}/node${index}/logs"
    mkdir -p "${ZOOKEEPER_CLUSTER_DIR}/node${index}/data"
    ZOOKEEPER_NODE_BASE_PATH="${ZOOKEEPER_CLUSTER_DIR}/node$index"
    ZOOKEEPER_NODE_DATA_PATH="${ZOOKEEPER_CLUSTER_DIR}/node$index/data"
    ZOOKEEPER_NODE_LOG_PATH="${ZOOKEEPER_CLUSTER_DIR}/node$index/logs"
    echo "ZOOKEEPER_NODE_BASE_PATH ==> $ZOOKEEPER_NODE_BASE_PATH"
    echo "ZOOKEEPER_NODE_DATA_PATH ==> $ZOOKEEPER_NODE_DATA_PATH"
    echo "ZOOKEEPER_NODE_LOG_PATH ==> $ZOOKEEPER_NODE_LOG_PATH"
    cd $ZOOKEEPER_NODE_BASE_PATH
    if [ $? -ne 0 ]; then
      echo "=====> cd dir [ ${ZOOKEEPER_NODE_BASE_PATH} ] failed"
    fi

    ## zookeeper config replace: myid、dataDir、dataLogDir、clientPort、server.1
    cp "${ZOOKEEPER_HOME}/conf/${SERVER_SAMPLE_FILE_NAME}" ./

    ## zookeeper node args replace
    sed -i "s|clientPort=.*|clientPort=${ZOOKEEPER_NODE_CLIENT_PORT_PREFIX}${index}|g" $SERVER_SAMPLE_FILE_NAME
    sed -i "s|dataDir=.*|dataDir=${ZOOKEEPER_NODE_DATA_PATH}|g" $SERVER_SAMPLE_FILE_NAME
    echo "dataLogDir=${ZOOKEEPER_NODE_LOG_PATH}" >> $SERVER_SAMPLE_FILE_NAME

    cd ${ZOOKEEPER_NODE_DATA_PATH}
    echo $index > myid

    ## server.id config
    for i in `seq $index`
    do
        for j in `seq $index`
        do
          nodeConfigPath="${ZOOKEEPER_CLUSTER_DIR}/node${j}/${SERVER_SAMPLE_FILE_NAME}"
          result=`grep "server.${i}" $nodeConfigPath | head -n 1`
          if [ -x $result ]; then
            echo "server.${i}=`hostname`:${ZOOKEEPER_NODE_SELECTION_PORT_PREFIX}${i}:${ZOOKEEPER_NODE_COMMUNICATION_PORT_PREFIX}${i}" >> $nodeConfigPath
          fi
        done
    done

    ## 节点配置
    ## bootstrap single node
    nohup zkServer.sh start "${ZOOKEEPER_NODE_BASE_PATH}/${SERVER_SAMPLE_FILE_NAME}" > /dev/null &
    echo "===> finish bootstrap zookeeper ${index} node"
}

for i in `seq $ZOOKEEPER_NUM`
do
	bootstrapSingleNode $i
done

