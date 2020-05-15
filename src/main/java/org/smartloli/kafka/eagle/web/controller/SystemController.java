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
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.smartloli.kafka.eagle.web.pojo.RoleAuthorityInfo;
import org.smartloli.kafka.eagle.web.pojo.UserInfo;
import org.smartloli.kafka.eagle.web.pojo.UserRoleInfo;
import org.smartloli.kafka.eagle.web.service.RoleService;
import org.smartloli.kafka.eagle.web.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 管理员系统功能接口
 * @author smartloli.
 * Created by May 26, 2017.
 */
@Controller
@RequestMapping("/system")
@Api("系統服务")
public class SystemController {

	@Autowired
	private RoleService roleService;
	@Autowired
	private UserInfoService userInfoService;

	@GetMapping("/role")
    @ApiOperation("跳转角色界面")
	public String roleView() {
        return "/system/role";
	}

	@GetMapping("/user")
    @ApiOperation("跳转用户视图")
	public String userView() {
        return "/system/user";
	}

	/** Notice viewer. */
	@GetMapping("/notice")
    @ApiOperation("跳转通知页面")
	public String noticeView() {
        return "/system/notice";
	}

	@PostMapping("/user/add")
    @ApiOperation("添加用户")
	public String addUser(HttpServletRequest request) {
		String rtxno = request.getParameter("ke_rtxno_name");
		String username = request.getParameter("ke_user_name");
		String realname = request.getParameter("ke_real_name");
		String email = request.getParameter("ke_user_email");

		UserInfo signin = new UserInfo();
		signin.setEmail(email);
		signin.setPassword(UUID.randomUUID().toString().substring(0, 8));
		signin.setRealname(realname);
		signin.setRtxno(Integer.parseInt(rtxno));
		signin.setUsername(username);
		try {
			return userInfoService.insertUser(signin) > 0 ? "redirect:/system/user" : "redirect:/500";
		} catch (Exception ex) {
			ex.printStackTrace();
			return "redirect:/500";
		}
	}

	@PostMapping("/user/modify")
    @ApiOperation("修改用户信息")
	public String modifyUser(HttpServletRequest request) {
		String username = request.getParameter("ke_user_name_modify");
		String realname = request.getParameter("ke_real_name_modify");
		String email = request.getParameter("ke_user_email_modify");
		String id = request.getParameter("ke_user_id_modify");

		UserInfo signin = new UserInfo();
		signin.setId(Integer.parseInt(id));
		signin.setEmail(email);
		signin.setRealname(realname);
		signin.setUsername(username);
		return userInfoService.modify(signin) > 0 ? "redirect:/system/user" : "redirect:/500";
	}

	@PostMapping("/user/reset")
    @ApiOperation("重置用户信息")
	public String resetUser(HttpServletRequest request) {
		String password = request.getParameter("ke_user_new_pwd_reset");
		String rtxnode = request.getParameter("ke_user_rtxno_reset");

		UserInfo signin = new UserInfo();
		signin.setRtxno(Integer.parseInt(rtxnode));
        signin.setPassword(password);
		if (userInfoService.resetPassword(signin) > 0) {
			return "redirect:/system/user";
		} else {
			return "redirect:/500";
		}
	}

	@GetMapping("/user/signin/rtxno/ajax")
    @ResponseBody
    @ApiOperation("获取自动生成的用户账号")
	public String getUserRtxNo() {
		return userInfoService.getAutoUserRtxNo();

	}

	/** Delete user. */
	@GetMapping("/user/delete/{id}")
    @ApiOperation("删除账号")
	public String deleteUser(@PathVariable("id") int id) {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(id);
		return userInfoService.delete(userInfo) > 0 ? "redirect:/system/user" : "redirect:/500";
	}

	/** Get the roles that the user owns. */
	@GetMapping(value = "/user/role/table/ajax")
    @ResponseBody
    @ApiOperation("差和讯用户角色信息")
	public String getUserRoleAjax(HttpServletRequest request) {
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

		Map<String, Object> map = new HashMap<>();
		map.put("search", search);
		map.put("start", iDisplayStart);
		map.put("size", iDisplayLength);

		JSONArray roles = JSON.parseArray(userInfoService.findUserBySearch(map).toString());
		JSONArray aaDatas = new JSONArray();

		for (Object object : roles) {
			JSONObject role = (JSONObject) object;
			int id = role.getInteger("id");
			JSONObject obj = new JSONObject();
			obj.put("rtxno", role.getString("rtxno"));
			obj.put("username", role.getString("username"));
			obj.put("realname", role.getString("realname"));
			obj.put("email", role.getString("email"));
			obj.put("password", role.getString("password"));

			if (KafkaConstants.ADMIN.equals(role.getString("username"))) {
				obj.put("operate", "");
			} else {
				obj.put("operate",
						"<div class='btn-group'><button class='btn btn-primary btn-xs dropdown-toggle' type='button' data-toggle='dropdown' aria-haspopup='true' aria-expanded='false'>Action <span class='caret'></span></button><ul class='dropdown-menu dropdown-menu-right'><li><a id='operater_modal' name='operater_modal' href='#"
								+ id + "/'><i class='fa fa-fw fa-adn'></i>Assign</a></li><li><a name='operater_reset_modal' href='#" + id + "'><i class='fa fa-fw fa-gear'></i>Reset</a></li><li><a name='operater_modify_modal' href='#" + id + "'><i class='fa fa-fw fa-edit'></i>Modify</a></li><li><a href='/system/user/delete/" + id
								+ "/'><i class='fa fa-fw fa-trash-o'></i>Delete</a></li></ul></div>");
			}
			aaDatas.add(obj);
		}

		int count = userInfoService.userCounts();
		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Get all the roles of the system. */
	@SuppressWarnings("unused")
	@GetMapping("/role/table/ajax")
    @ResponseBody
    @ApiOperation("获取所有角色信息")
	public String getRolesAjax(HttpServletResponse response, HttpServletRequest request) {
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

		JSONArray roles = JSON.parseArray(roleService.getRoles().toString());
		JSONArray aaDatas = new JSONArray();
		for (Object object : roles) {
			JSONObject role = (JSONObject) object;
			JSONObject obj = new JSONObject();
			obj.put("name", role.getString("roleName"));
			obj.put("describer", role.getString("roleDescriber"));
			obj.put("operate", "<a id='operater_modal' name='operater_modal' href='#" + role.getInteger("id") + "' class='btn btn-primary btn-xs'>Setting</a>");
			aaDatas.add(obj);
		}

		int count = roles.size();
		JSONObject target = new JSONObject();
		target.put("sEcho", sEcho);
		target.put("iTotalRecords", count);
		target.put("iTotalDisplayRecords", count);
		target.put("aaData", aaDatas);
		return target.toJSONString();
	}

	/** Obtain the resources it owns through the role id. */
	@GetMapping("/role/resource/{roleId}/ajax")
    @ResponseBody
    @ApiOperation("获取角色拥有权限")
	public String roleResourceAjax(@PathVariable("roleId") int roleId) {
		return roleService.getRoleTree(roleId);
	}

	/** Find siginer through the user id. */
	@GetMapping(value = "/user/signin/{id}/ajax")
    @ResponseBody
    @ApiOperation("查询用户信息")
	public String findUserByIdAjax(@PathVariable("id") int id) {
		return userInfoService.findUserById(id);
	}

	/** Change the user's role. */
	@GetMapping("/user/role/{action}/{userId}/{roleId}/ajax")
    @ResponseBody
    @ApiOperation("修改用户角色")
	public String  changeUserRoleAjax(@PathVariable("action") String action, @PathVariable("userId") int userId, @PathVariable("roleId") int roleId, HttpServletResponse response, HttpServletRequest request) {
			UserRoleInfo userRoleInfo = new UserRoleInfo();
			userRoleInfo.setUserId(userId);
			userRoleInfo.setRoleId(roleId);
			JSONObject object = new JSONObject();
			int code = 0;
			if ("add".equals(action)) {
				code = roleService.insertUserRole(userRoleInfo);
				if (code > 0) {
					object.put("info", "Add role has successed.");
				} else {
					object.put("info", "Add role has failed.");
				}
			} else if ("delete".equals(action)) {
				code = roleService.deleteUserRole(userRoleInfo);
				if (code > 0) {
					object.put("info", "Delete role has successed.");
				} else {
					object.put("info", "Delete role has failed.");
				}
			}
			object.put("code", code);
			return object.toJSONString();
	}

	/** Get the corresponding roles through the user id. */
	@GetMapping("/user/role/{userId}/ajax")
    @ResponseBody
    @ApiOperation("查询用户角色")
	public String userRoleAjax(@PathVariable("userId") int userId) {
        JSONObject object = new JSONObject();
        object.put("role", roleService.getRoles());
        object.put("userRole", roleService.findRoleByUserId(userId));
        return object.toJSONString();
	}

	/** Change the resources that you have by role id. */
	@GetMapping("/role/{action}/{roleId}/{resourceId}/")
    @ResponseBody
    @ApiOperation("修改角色权限")
	public String changeRoleResource(@PathVariable("action") String action, @PathVariable("roleId") int roleId, @PathVariable("resourceId") int resourceId, HttpServletResponse response) {
			JSONObject object = new JSONObject();
			RoleAuthorityInfo roleAuthorityInfo = new RoleAuthorityInfo();
			roleAuthorityInfo.setRoleId(roleId);
			roleAuthorityInfo.setResourceId(resourceId);
			if ("insert".equals(action)) {
				int code = roleService.insertRoleResource(roleAuthorityInfo);
				object.put("code", code);
				if (code > 0) {
					object.put("info", "Add role has successed.");
				} else {
					object.put("info", "Add role has failed.");
				}
			} else if ("delete".equals(action)) {
				int code = roleService.deleteRoleResource(roleAuthorityInfo);
				object.put("code", code);
				if (code > 0) {
					object.put("info", "Delete role has successed.");
				} else {
					object.put("info", "Delete role has failed.");
				}
			}
			return object.toJSONString();
	}

}
