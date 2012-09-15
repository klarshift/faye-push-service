package com.klarshift.grails.plugins.pushservice

import grails.converters.JSON
import groovy.json.JsonSlurper

/**
 * push service
 * @author timo
 * 
 * TODO: SSL, encryption
 * TODO: response status on publish
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
	 * and ooptional channel
	 * @param endpointName
	 * @return
	 */
	public boolean isOnline(String endpointName, String channelName = null){		
		FayeEndpoint e = getEndpoint(endpointName)
		if(!e)return false		
		if(!e.active)return false
		if(!e.online)return false
		if(channelName){
			FayeChannel c = getChannel(e, channelName)
			if(!c)return false
			if(!c.active)return false
		}		
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
		log.info("Created endpoint `$endpoint`")
		return endpoint
	}

	/**
	 * create a faye channel
	 * @param endpoint
	 * @param name
	 * @return
	 */
	public FayeChannel createChannel(FayeEndpoint endpoint, String name, boolean active = true){
		FayeChannel c = new FayeChannel(name: name, endpoint: endpoint)
		endpoint.addToChannels(c)
		log.info("Added channel `$c`")
		return c
	}
	
	/**
	 * delete a faye channel
	 * @param endpoint
	 * @param channel
	 * @return
	 */
	public boolean deleteChannel(FayeEndpoint endpoint, FayeChannel channel){
		endpoint.removeFromChannels(channel)
		endpoint.save(flush: true)
		channel.delete(flush: true)
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
	 * get channel by endpoint and name
	 * @param endpoint
	 * @param channelName
	 * @return
	 */
	public FayeChannel getChannel(FayeEndpoint endpoint, String channelName){
		if(channelName.startsWith('/session/')){
			return new FayeChannel(name: channelName, endpoint: endpoint)
		}
		return FayeChannel.findByEndpointAndName(endpoint, channelName)
	}

	/**
	 * publish data to endpoint channel
	 * @param endpointName
	 * @param channelName
	 * @param data
	 * @return
	 */
	public boolean publish(String endpointName, String channelName, data) {
		// get service
		FayeEndpoint endpoint = getEndpoint(endpointName)
		if(!endpoint){
			log.error("Service [$endpointName] not found.")
			return false
		}

		// check service online state
		if(!endpoint.active){
			log.info("Service [$endpointName] is not active. Will not push to that service.")
			return false
		}
		if(!endpoint.online){
			log.error("Service [$endpointName] is OFFLINE.")
			return false
		}
		
		// get channel
		FayeChannel channel = getChannel(endpoint, channelName)
		if(!channel){
			log.error("Channel [$channelName] for service [$endpointName] not found.")
			return false
		}

		// check channel state
		if(!channel.active){
			// do nothing
			log.debug("Channel [$channelName] for service [$endpointName] deactivated.")
			return false
		}

		// pack packet
		def packet = [channel: channel.name, data: data]
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
