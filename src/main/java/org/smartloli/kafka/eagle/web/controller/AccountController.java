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
import lombok.extern.slf4j.Slf4j;
import org.smartloli.kafka.eagle.web.pojo.UserInfo;
import org.smartloli.kafka.eagle.web.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户账户控制器(登陆、登出、重置名密码)
 * @author smartloli.
 * Created by May 26, 2017.
 */
@Controller
@RequestMapping("/account")
@Api("账户控制器")
@Slf4j
public class AccountController {

	@Autowired
	private UserInfoService userInfoService;

	/** 重置密码 */
	@PostMapping(value = "/resetPassword")
    @ApiOperation("重置密码")
	public String resetPassword(HttpServletRequest request, @AuthenticationPrincipal UserDetails userDetails) {
		String password = request.getParameter("ke_new_password_name");
		UserInfo signin = new UserInfo();
		signin.setUsername(userDetails.getUsername());
		signin.setPassword(password);
		return userInfoService.resetPassword(signin) > 0 ? "redirect:/account/signout" : "redirect:/500";
	}
}
