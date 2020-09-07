package org.smartloli.kafka.eagle.web.config;

import com.alibaba.fastjson.JSONObject;
import org.smartloli.kafka.eagle.web.constant.CommonConstants;

import java.io.Serializable;

/**
 * 通用响应体
 */
public class ResponseMsg implements Serializable {

    private String code;
    private Object data;
    private String message;

    public ResponseMsg() {
        super();
    }

    public ResponseMsg(String code, Object data, String message) {
        super();
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public String getCode() {
        return code == null ? "" : code.trim();
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 创建成功返回消息
     *
     * @return
     */
    public static ResponseMsg buildSuccessResponse() {
        return buildSuccessResponse(null);
    }

    /**
     * 创建成功返回消息
     *
     * @param data
     * @return
     */
    public static ResponseMsg buildSuccessResponse(Object data) {
        return buildResponse(CommonConstants.SUCCESS_CODE, CommonConstants.SUCCESS_MSG, data);
    }

    /**
     * 创建错误返回消息
     *
     * @return
     */
    public static ResponseMsg buildFailureResponse() {
        return buildResponse(CommonConstants.FAILURE_CODE, CommonConstants.FAILURE_MSG, null);
    }

    /**
     * 创建错误返回消息
     *
     * @param data
     * @return
     */
    public static ResponseMsg buildFailureResponse(Object data) {
        return buildResponse(CommonConstants.FAILURE_CODE, CommonConstants.FAILURE_MSG, data);
    }

    /**
     * 创建错误返回消息
     *
     * @param errMsg
     * @return
     */
    public static ResponseMsg buildFailureResponse(String errMsg, Object data) {
        return buildResponse(CommonConstants.FAILURE_CODE, errMsg, data);
    }

    /**
     * 创建返回消息
     *
     * @param msg
     * @return
     */
    public static ResponseMsg buildResponse(String code, String msg, Object data) {
        return new ResponseMsg(code, data, msg);
    }

    @Override
    public String toString() {
        return JSONObject.toJSONString(this);
    }

}
