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

import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.smartloli.kafka.eagle.web.constant.HttpConstants;
import org.smartloli.kafka.eagle.web.constant.KafkaConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpSession;

/**
 * 页面跳转控制器
 * @author smartloli.
 * Created by Sep 6, 2016
 */
@Controller
public class PageController {

    @GetMapping(HttpConstants.LOGIN_URL)
    @ApiOperation("登陆页")
    public String login(HttpSession httpSession) {
        //预防登陆认证失败页面错误信息无法显示问题
        if(null == httpSession.getAttribute(KafkaConstants.ERROR_DISPLAY)) {
            httpSession.setAttribute(KafkaConstants.ERROR_DISPLAY, false);
            httpSession.setAttribute(KafkaConstants.ERROR_LOGIN, StringUtils.EMPTY);
        }
        return "/account/signin";
    }

    /**
     * 跳转面板主页
     * @return
     */
    @GetMapping({HttpConstants.INDEX_URL, HttpConstants.ROOT_URL})
    @ApiOperation("跳转主页")
    public String toIndex() {
        return "/main/index";
    }

	/** 403 error page viewer. */
	@GetMapping(HttpConstants.ERROR_403)
	public String e403() {
        return "/error/403";
	}
	
	/** 404 error page viewer. */
	@GetMapping(HttpConstants.ERROR_404)
	public String e404() {
        return "/error/404";
	}

	/** 405 error page viewer. */
	@GetMapping(HttpConstants.ERROR_405)
	public String e405() {
        return "/error/405";
	}

	/** 500 error page viewer. */
	@GetMapping(HttpConstants.ERROR_500)
	public String e500() {
        return "/error/500";
	}

	/** 503 error page viewer. */
	@GetMapping(HttpConstants.ERROR_503)
	public String e503() {
        return "/error/503";
	}
}
