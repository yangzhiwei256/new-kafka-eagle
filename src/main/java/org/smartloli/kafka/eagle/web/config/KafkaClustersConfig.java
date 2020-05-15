package org.smartloli.kafka.eagle.web.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zhiwei_yang
 * @time 2020-5-14-14:59
 */
@Configuration
@ConfigurationProperties(prefix = "kafka.eagle")
@Data
public class KafkaClustersConfig {

    private List<SingleClusterConfig> clusters;

    /**
     * 获取Kafka集群名称列表
     *
     * @return
     */
    public List<String> getClusterAllAlias() {
        if (CollectionUtils.isEmpty(clusters)) {
            return Collections.emptyList();
        }
        return clusters.stream().map(SingleClusterConfig::getAlias).collect(Collectors.toList());
    }

    /**
     * 根据kafka集群名称获取配置
     *
     * @param clusterName
     * @return
     */
    public SingleClusterConfig getClusterConfigByName(String clusterName) {
        if (StringUtils.isEmpty(clusterName)) {
            return null;
        }
        for (SingleClusterConfig singleClusterConfig : clusters) {
            if (singleClusterConfig.getAlias().equalsIgnoreCase(clusterName)) {
                return singleClusterConfig;
            }
        }
        return null;
    }
}
