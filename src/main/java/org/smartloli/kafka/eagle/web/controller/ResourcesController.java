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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.smartloli.kafka.eagle.web.service.AuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * System resource management is used to control access to each module.
 * 
 * @author smartloli.
 *
 *         Created by May 26, 2017.
 */
@Controller
@RequestMapping("/system")
@Api("资源控制器")
public class ResourcesController {

	@Autowired
	private AuthorityService authorityService;

	/** Resource viewer. */
	@RequestMapping(value = "/resource", method = RequestMethod.GET)
	public ModelAndView resourceView(HttpSession session) {
		ModelAndView mav = new ModelAndView();
		mav.setViewName("/system/resource");
		return mav;
	}

	/** Resource graph. */
	@GetMapping("/resource/graph")
    @ResponseBody
    @ApiOperation("资源视图")
	public String resGraphAjax() {
		return authorityService.getResourcesTree();
	}

	/** Add home resource form. */
	@RequestMapping(value = "/resource/add/home/", method = RequestMethod.POST)
	public String addHome(HttpServletRequest request) {
		String name = request.getParameter("ke_resource_home_name");
		String url = request.getParameter("ke_resource_home_url");
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("url", url);
		map.put("parentId", -1);
		authorityService.insertResource(map);

		return "redirect:/system/resource";
	}

	/** Add children resource form. */
	@RequestMapping(value = "/resource/add/children/", method = RequestMethod.POST)
	public String addChildren(HttpServletRequest request) {
		String name = request.getParameter("ke_resource_child_name");
		String url = request.getParameter("ke_resource_child_url");
		String parentId = request.getParameter("res_parent_id");
		Map<String, Object> map = new HashMap<>();
		map.put("name", name);
		map.put("url", url);
		map.put("parentId", parentId);
		int code = authorityService.insertResource(map);
		if (code > 0) {
			return "redirect:/system/resource";
		} else {
			return "redirect:/500";
		}

	}

	/** Delete resource parent or children. */
	@RequestMapping(value = "/resource/delete/parent/or/children/", method = RequestMethod.POST)
	public String deleteParentOrChildren(HttpServletRequest request) {
		String res_child_root_id = request.getParameter("res_child_root_id");
		String res_child_id = request.getParameter("res_child_id");
		int resId = 0;
		Map<String, Object> map = new HashMap<>();
		if (res_child_id == null) {
			resId = Integer.parseInt(res_child_root_id);
			map.put("parentId", resId);
		} else {
			resId = Integer.parseInt(res_child_id);
			map.put("resourceId", resId);
		}
		int code = authorityService.deleteParentOrChildByResId(map);
		if (code > 0) {
			return "redirect:/system/resource";
		} else {
			return "redirect:/500";
		}

	}

	/** Get parent resource. */
	@GetMapping("/resource/parent")
    @ResponseBody
    @ApiOperation("获取父类资源")
	public String resParentAjax() {
		return  authorityService.getResourceParent().toString();
	}

	/** Get children resource by parent id. */
	@GetMapping("/resource/child/{parentId}")
    @ResponseBody
    @ApiOperation("获取子类资源")
	public String resChildAjax(@PathVariable("parentId") int parentId) {
		return authorityService.findResourceByParentId(parentId).toString();
	}
}
