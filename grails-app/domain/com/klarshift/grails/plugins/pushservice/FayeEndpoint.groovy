package com.klarshift.grails.plugins.pushservice

/**
 * faye endpoint
 * @author timo
 *
 */
class FayeEndpoint {
	String name
	String publishUrl
	boolean active = true
	boolean online = false
	Date lastCheck = null
	
	static hasMany = [channels : FayeChannel]

    static constraints = {
		name nullable: false, blank: false
		publishUrl nullable: false
		active nullable: false
		online nullable: false
		lastCheck nullable: true		
    }
	
	public String toString(){
		"$name:$publishUrl"
	}
}
