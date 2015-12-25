package com.example.nettybook;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

public abstract class ApiRequestTemplate implements ApiRequest {
	protected Logger logger;
	protected Map<String,String> reqData;
	protected JsonObject apiResult;
	
	public ApiRequestTemplate(Map<String,String> reqData) { //1
		this.logger = LogManager.getLogger(this.getClass());
		this.apiResult = new JsonObject();
		this.reqData = reqData;
	}
	public void executeService() {
		try{
			this.requestParamValidation();//2
			this.service();//3
			
		} catch (RequestParamException e) {
			logger.error(e);
			this.apiResult.addProperty("resultCode", "405");
		} catch (ServiceException e) {
			logger.error(e);
			this.apiResult.addProperty("resultCode", "501");
		}
	}

	public JsonObject getApiResult() {
		return this.apiResult; //4
	}


}
