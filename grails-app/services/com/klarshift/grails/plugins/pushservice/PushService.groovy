package com.klarshift.grails.plugins.pushservice

import grails.converters.JSON
import groovy.json.JsonSlurper

/**
 * push service
 * @author timo@klarshift.de
 *
 */
class PushService {
	
	/**
	 * check all services
	 */
	public void checkServices(){
		// loop all endpoints and check them
		FayeEndpoint.findAllWhere([active: true]).each{ FayeEndpoint e ->
			checkEndpoint(e)
		}		
	}
	
	/**
	 * check online state of an endpoint
	 * @param endpointName
	 * @return
	 */
	public boolean isOnline(String endpointName){		
		FayeEndpoint e = getEndpoint(endpointName)
		if(!e)return false		
		if(!e.active)return false
		if(!e.online)return false
		return true
	}
	
	/**
	 * check a given endpoint
	 * @param endpoint
	 * @return
	 */
	public boolean checkEndpoint(FayeEndpoint endpoint){
		endpoint.lastCheck = new Date()
		endpoint.online = pingEndpoint(endpoint)
		endpoint.save(flush: true)		
		return endpoint.online
	}
	
	/**
	 * ping endpoint
	 * @param endpoint
	 * @return
	 */
	public boolean pingEndpoint(FayeEndpoint endpoint){
		try{						
			URL url = new URL(endpoint.publishUrl)
			HttpURLConnection con = url.openConnection()
			con.setReadTimeout(1000)
			con.setConnectTimeout(1000)
			con.setRequestMethod("GET")
			int rc = con.getResponseCode()
			con.disconnect()
			if(rc == 200){
				return true
			}			
		}catch(Exception e){ }		
		return false
	}
	
	/**
	 * delete an endpoint by name
	 * @param name
	 */
	public boolean deleteEndpoint(String name){
		FayeEndpoint endpoint = FayeEndpoint.findByName(name)
		if(endpoint){
			endpoint.delete(flush: true)
			return true
		}
		return false
	}

	/**
	 * create endpoint with given name
	 * and service url
	 * @param name
	 * @param publishUrl
	 * @return
	 */
	public FayeEndpoint createEndpoint(String name, String publishUrl){
		FayeEndpoint endpoint = new FayeEndpoint(name: name, publishUrl: publishUrl)
		if(endpoint.save(flush: true)){
			log.info("Created endpoint `$endpoint`")
			return endpoint
		}
		return null
	}
	
	/**
	 * get service by service name
	 * @param serviceName
	 * @return
	 */
	public FayeEndpoint getEndpoint(String serviceName){
		return FayeEndpoint.findByName(serviceName)
	}

	/**
	 * publish data to endpoint channel
	 * @param endpointName
	 * @param channelName
	 * @param data
	 * @return
	 */
	public boolean publish(String endpointName, String channelName, data) {
		// check online state
		if(!isOnline(endpointName)){
			log.error("Service [$endpointName] is OFFLINE.")
			return false
		}	
		
		FayeEndpoint endpoint = getEndpoint(endpointName)	

		// pack packet
		def packet = [channel: channelName, data: data]
		try{
			String json = (packet as JSON).toString()			
			URL url = new URL(endpoint.publishUrl)
			HttpURLConnection con = url.openConnection()
			con.setReadTimeout(1000)
			con.setConnectTimeout(1000)
			con.setRequestMethod("POST")
			con.setRequestProperty("Content-Type", "application/json; utf-8")
			con.setDoOutput(true)
			con.getOutputStream().write(json.getBytes('utf-8'))
			InputStream is = con.getInputStream();			
			con.disconnect()			
		}catch(Exception e){
			e.printStackTrace()
			log.error "Could not push: ${e.message}"
			return false
		}
		
		return true
	}	
}
