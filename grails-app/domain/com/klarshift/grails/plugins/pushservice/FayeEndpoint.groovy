package com.klarshift.grails.plugins.pushservice

/**
 * faye endpoint
 * 
 * @author timo@klarshift.de
 *
 */
class FayeEndpoint {
	String name
	String publishUrl
	boolean active = true
	boolean online = false
	Date lastCheck = null	

    static constraints = {
		name unique: true, nullable: false, blank: false
		publishUrl nullable: false
		active nullable: false
		online nullable: false
		lastCheck nullable: true		
    }
	
	static mapping = {
		cache true
	}
	
	public String toString(){
		"$name:$publishUrl"
	}
}
