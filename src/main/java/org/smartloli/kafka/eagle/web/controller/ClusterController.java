/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.smartloli.kafka.eagle.web.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.smartloli.kafka.eagle.web.config.KafkaClustersConfig;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.service.ClusterService;
import org.smartloli.kafka.eagle.web.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * kafka集群控制器
 * @author smartloli.
 * Created by Sep 6, 2016.
 * Update by hexiang 20170216
 */
@Controller
@Api("Kafka集群控制器")
public class ClusterController {

    @Autowired
    private ClusterService clusterService;

    @Autowired
    private KafkaClustersConfig kafkaClustersConfig;

    @GetMapping(value = "/cluster/info")
    @ApiOperation("跳转集群页")
    public String clusterView() {
        return "/cluster/cluster";
    }

    @GetMapping("/cluster/multi")
    @ApiOperation("跳转多集群页")
    public String clustersView() {
		return "/cluster/multicluster";
	}

	@GetMapping(value = "/cluster/zkcli")
    @ApiOperation("kafka客户端")
	public String zkCliView() {
		return "/cluster/zkcli";
	}

	/** Get cluster data by ajax. */
	@GetMapping("/cluster/info/{type}/ajax")
    @ResponseBody
    @ApiOperation("获取集群数据")
	public String clusterAjax(@PathVariable("type") String type, HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			} else if ("sSearch".equals(param.getString("name"))) {
				search = param.getString("value");
			}
		}

		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();

		JSONObject deserializeClusters = JSON.parseObject(clusterService.get(clusterAlias, type));
		JSONArray clusters = deserializeClusters.getJSONArray(type);
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : clusters) {
			JSONObject cluster = (JSONObject) object;
			if (search.length() > 0 && search.equals(cluster.getString("host"))) {
				JSONObject obj = new JSONObject();
				obj.put("id", cluster.getInteger("id"));
				obj.put("port", cluster.getInteger("port"));
				obj.put("ip", cluster.getString("host"));
				if ("kafka".equals(type)) {
					obj.put("jmxPort", cluster.getInteger("jmxPort"));
					obj.put("created", cluster.getString("created"));
					obj.put("modify", cluster.getString("modify"));
					String version = cluster.getString("version") == "" ? KafkaConstants.UNKOWN : cluster.getString("version");
					if (KafkaConstants.UNKOWN.equals(version)) {
						obj.put("version", "<a class='btn btn-danger btn-xs'>" + version + "</a>");
					} else {
						obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
					}
				} else if ("zk".equals(type)) {
					String mode = cluster.getString("mode");
					if ("death".equals(mode)) {
						obj.put("mode", "<a class='btn btn-danger btn-xs'>" + mode + "</a>");
					} else {
						obj.put("mode", "<a class='btn btn-success btn-xs'>" + mode + "</a>");
					}
					String version = cluster.getString("version");
					if (StrUtils.isNull(version)) {
						obj.put("version", "<a class='btn btn-danger btn-xs'>unkown</a>");
					} else {
						obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
					}
				}
				aaDatas.add(obj);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject obj = new JSONObject();
					obj.put("id", cluster.getInteger("id"));
					obj.put("port", cluster.getInteger("port"));
					obj.put("ip", cluster.getString("host"));
					if ("kafka".equals(type)) {
						obj.put("jmxPort", cluster.getInteger("jmxPort"));
						obj.put("created", cluster.getString("created"));
						obj.put("modify", cluster.getString("modify"));
						String version = cluster.getString("version") == "" ? KafkaConstants.UNKOWN : cluster.getString("version");
						if (KafkaConstants.UNKOWN.equals(version)) {
							obj.put("version", "<a class='btn btn-danger btn-xs'>" + version + "</a>");
						} else {
							obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
						}
					} else if ("zk".equals(type)) {
						String mode = cluster.getString("mode");
						if ("death".equals(mode)) {
							obj.put("mode", "<a class='btn btn-danger btn-xs'>" + mode + "</a>");
						} else {
							obj.put("mode", "<a class='btn btn-success btn-xs'>" + mode + "</a>");
						}
						String version = cluster.getString("version");
						if (StrUtils.isNull(version)) {
							obj.put("version", "<a class='btn btn-danger btn-xs'>unkown</a>");
						} else {
							obj.put("version", "<a class='btn btn-success btn-xs'>" + version + "</a>");
						}
					}
					aaDatas.add(obj);
				}
				offset++;
			}
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", clusters.size());
		target.put("iTotalDisplayRecords", clusters.size());
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	@GetMapping("/cluster/info/{clusterAlias}/change")
    @ApiOperation("切换集群")
	public ModelAndView clusterChangeAjax(@PathVariable("clusterAlias") String clusterAlias, HttpSession session) {
		if (!clusterService.hasClusterAlias(clusterAlias)) {
			return new ModelAndView("redirect:/error/404");
		} else {
            session.removeAttribute(KafkaConstants.CLUSTER_ALIAS);
            session.setAttribute(KafkaConstants.CLUSTER_ALIAS, clusterAlias);
            String dropList = "<ul class='dropdown-menu'>";
            int i = 0;
            for (String clusterAliasStr : kafkaClustersConfig.getClusterAllAlias()) {
                if (!clusterAliasStr.equals(clusterAlias) && i < KafkaConstants.CLUSTER_ALIAS_LIST_LIMIT) {
                    dropList += "<li><a href='/cluster/info/" + clusterAliasStr + "/change'><i class='fa fa-fw fa-sitemap'></i>" + clusterAliasStr + "</a></li>";
                    i++;
                }
            }
            dropList += "<li><a href='/cluster/multi'><i class='fa fa-fw fa-tasks'></i>More...</a></li></ul>";
            session.removeAttribute(KafkaConstants.CLUSTER_ALIAS_LIST);
            session.setAttribute(KafkaConstants.CLUSTER_ALIAS_LIST, dropList);
            return new ModelAndView("redirect:/");
        }
	}

	@GetMapping("/cluster/info/multicluster/ajax")
    @ResponseBody
    @ApiOperation("获取多集群信息")
	public String multiClusterAjax(HttpServletRequest request) {
		String aoData = request.getParameter("aoData");
		JSONArray params = JSON.parseArray(aoData);
		int sEcho = 0, iDisplayStart = 0, iDisplayLength = 0;
		String search = "";
		for (Object object : params) {
			JSONObject param = (JSONObject) object;
			if ("sEcho".equals(param.getString("name"))) {
				sEcho = param.getIntValue("value");
			} else if ("iDisplayStart".equals(param.getString("name"))) {
				iDisplayStart = param.getIntValue("value");
			} else if ("iDisplayLength".equals(param.getString("name"))) {
				iDisplayLength = param.getIntValue("value");
			} else if ("sSearch".equals(param.getString("name"))) {
				search = param.getString("value");
			}
		}

		JSONArray clusterAliass = clusterService.clusterAliass();
		int offset = 0;
		JSONArray aaDatas = new JSONArray();
		for (Object object : clusterAliass) {
			JSONObject cluster = (JSONObject) object;
			if (search.length() > 0 && cluster.getString("clusterAlias").contains(search)) {
				JSONObject target = new JSONObject();
				target.put("id", cluster.getInteger("id"));
				target.put("clusterAlias", cluster.getString("clusterAlias"));
				target.put("zkhost", cluster.getString("zkhost"));
				target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='btn btn-primary btn-xs'>Change</a>");
				aaDatas.add(target);
			} else if (search.length() == 0) {
				if (offset < (iDisplayLength + iDisplayStart) && offset >= iDisplayStart) {
					JSONObject target = new JSONObject();
					target.put("id", cluster.getInteger("id"));
					target.put("clusterAlias", cluster.getString("clusterAlias"));
					target.put("zkhost", cluster.getString("zkhost"));
					target.put("operate", "<a name='change' href='#" + cluster.getString("clusterAlias") + "' class='btn btn-primary btn-xs'>Change</a>");
					aaDatas.add(target);
				}
				offset++;
			}
		}

		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", clusterAliass.size());
		target.put("iTotalDisplayRecords", clusterAliass.size());
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	@GetMapping("/cluster/zk/islive/ajax")
    @ResponseBody
    @ApiOperation("获取集群Zookeeper信息")
	public String zkCliLiveAjax(HttpServletRequest request) {
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return clusterService.status(clusterAlias).toJSONString();
	}

	@GetMapping("/cluster/zk/cmd/ajax")
    @ResponseBody
    @ApiOperation("指定Zookeeper命令")
	public String zkCliCmdAjax(HttpServletRequest request) {
		String cmd = request.getParameter("cmd");
		String type = request.getParameter("type");
		HttpSession session = request.getSession();
		String clusterAlias = session.getAttribute(KafkaConstants.CLUSTER_ALIAS).toString();
		return clusterService.execute(clusterAlias, cmd, type);
	}

}
