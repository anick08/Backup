/**
 *  Thermostat Boost
 *
 *  Copyright 2014 Tim Slagle
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

// Automatically generated. Make future change here.
definition(
    name: "Thermostat Boost",
    namespace: "tslagle13",
    author: "Tim Slagle",
    description: "Turn on the thermostat for a certain period of time and then back off after that time has expired.  Good for when you need to pull some moisture out of the air but don't want to forget to turn the thermostat off.",
    category: "Green Living",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")

preferences {
   page( name:"Thermostat", title:"Thermostat", nextPage:"mode", uninstall:true, install:false ) {
		section("Choose a thermostat to boost") {
   			input "thermostats1", "capability.thermostat", multiple: false
       }
  }
  
  page( name:"mode", title:"Mode", nextPage:"setpoints", uninstall:true, install:false ) {
  		section("If thermostat is off switch to which mode?") {
    		input "turnOnTherm", "enum", metadata: [values: ["cool", "heat"]], required: false
  		}
  }
  page( name:"setpoints", title:"Thermostat Setpoints", uninstall:true, install:true ) {
  	section("Set the thermostat to the following temps") {
    	input "coolingTemp", "decimal", title: "Cooling temp?", required: false
    	input "heatingTemp", "decimal", title: "Heating temp?", required: false
  	}
  	section("For how long?") {
    	input "turnOffDelay", "decimal", defaultValue:30
  	}
  }  
}  

def installed() {
subscribe(app, appTouch)
}

def updated() {
  subscribe(app, appTouch)
}

def appTouch(evt) {
	def currentCoolSetpoint = thermostats1.latestValue("coolingSetpoint")
    def currentHeatSetpoint = thermostats1.latestValue("heatingSetpoint")
    def currentMode = thermostats1.latestValue("thermostatMode")
	def mode = turnOnTherm
    state.currentCoolSetpoint = currentCoolSetpoint
    state.currentHeatSetpoint = currentHeatSetpoint
    state.currentMode = currentMode
    
    if(currentMode != "off"){
    	thermostats1.setCoolingSetpoint(coolingTemp)
    	thermostats1.setHeatingSetpoint(heatingTemp)
    }
    
    if(currentMode == "off") {
    	thermostats1."${mode}"()
    	thermostats1.setCoolingSetpoint(coolingTemp)
    	thermostats1.setHeatingSetpoint(heatingTemp)
    }
    
    thermoShutOffTrigger()
    log.debug("current coolingsetpoint is ${state.currentCoolSetpoint}")
    log.debug("current heatingsetpoint is ${state.currentHeatSetpoint}")
    log.debug("current mode is ${state.currentMode}")
    //thermostats.setCoolingSetpoint(currentCoolSetpoint)
}

def thermoShutOffTrigger() {
    log.info("Starting timer to turn off thermostat")
    def delay = (turnOffDelay != null && turnOffDelay != "") ? turnOffDelay * 60 : 60 
    state.turnOffTime = now()
	log.debug ("Turn off delay is ${delay}")
    runIn(delay, "thermoShutOff")
  }

def thermoShutOff(){
	log.info("Returning thermostat back to normal")
	thermostats1.setCoolingSetpoint(state.currentCoolSetpoint)
    thermostats1.setHeatingSetpoint(state.currentHeatSetpoint)
    thermostats1."${state.currentMode}"()
}