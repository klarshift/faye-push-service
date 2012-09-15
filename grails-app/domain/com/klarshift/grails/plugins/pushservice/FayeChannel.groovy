package com.klarshift.grails.plugins.pushservice

/**
 * faye channel
 * @author timo
 *
 */
class FayeChannel {
	String name	
	Boolean active = true
	
	static belongsTo = [endpoint: FayeEndpoint]

    static constraints = {
		name nullable: false, blank: false
		active nullable: true
    }
	
	public String toString(){
		"${endpoint}${name}"
	}
}
