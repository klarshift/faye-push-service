package com.klarshift.grails.plugins.pushservice

/**
 * push service taglib
 * @author timo
 *
 */
class PushServiceTagLib {
	static namespace = "ps"
	def pushService
	
	def ifOnline = { attrs, body ->
		if(pushService.isOnline(attrs.endpoint, attrs.channel ?: null)){
			out << body()
		} 
	}
	
	def clientUrl = { attrs ->
		FayeEndpoint e = pushService.getEndpoint(attrs.endpoint)
		out << e.publishUrl + "/faye"
	}
	
	def ifOffline = { attrs, body ->
		if(!pushService.isOnline(attrs.endpoint, attrs.channel)){
			out << body()
		}
	}
	
	def init = { attrs ->		
		if(pushService.isOnline(attrs.endpoint)){
			FayeEndpoint e = pushService.getEndpoint(attrs.endpoint)
			def src = "${e.publishUrl}/faye/client.js"
			out << "<script type=\"text/javascript\" src=\"$src\"></script>"		
		}
	}
}
