require 'sinatra'
require 'json'
require 'securerandom'

# Sets the server to listen on all available interfaces
set :bind,'0.0.0.0'
# sets root as the parent-directory of the current file
set :root, File.join(File.dirname(__FILE__), '..')
# sets the view directory correctly
set :views, Proc.new { File.join(root, "views") } 

enrolled_devices=Array.new

def isRegistered(enrolled_devices,uuid)
	return enrolled_devices.include?uuid
end

# Returns the testcase from the file as a JSON
# Assumes that commands.json contains correctly formated JSON such as this:
# {
# 	"steps": (Array<Strings>) [<steps 1 ... n>]
# }

def getTestCase(filename) 
	file = File.read(filename)
	puts "RETURNING JSON FROM FILE:" + JSON.parse(file).to_json
	File.delete(filename)
	return JSON.parse(file)
end

# RegisterDevice [POST REQUEST]
# NOTE: Case Sensitivity is Important
# Request Sample:
# {
# 	"uuid": (String)<device id>
# }
#
# Status:
# Success if the device was registered successfully, 
# Fail if the request was invalid (ie, no 'uuid' key), 
# Exists if the 'uuid' is already registered
#
# Enrolled:
# returns device is enrolled or not
#
# Response Sample:
# {
# 	"status":(string)<"success","fail","exists">, 
# 	"enrolled":(boolean)<true,false> 
# }

post '/RegisterDevice' do 
	#Building the response hash that will be returned as a JSON
	response={}
	invalidRequest=false

	begin # In the case of an invalid request
		@requestFromDevice=JSON.parse(request.body.read)
		puts "Request JSON: " + @requestFromDevice.to_s
	rescue
		invalidRequest=true
	end

	if !invalidRequest # Validating the request	
		if @requestFromDevice.is_a?(::Hash) && @requestFromDevice.has_key?("uuid") && !@requestFromDevice["uuid"].empty? # Request is valid
			if isRegistered(enrolled_devices, @requestFromDevice["uuid"]) # uuid is already registered		
				response[:status]="exists" 
			else
				enrolled_devices.push(@requestFromDevice["uuid"]) # adding uuid to the enrolled devices list
				response[:status]="success"
			end
		else # Request is invalid
			puts "Invalid Request JSON: " + @requestFromDevice.to_s
			response[:status]="fail"
		end	

		if enrolled_devices.include? @requestFromDevice["uuid"] #device is in the enrolled list	
			response[:enrolled]=true
		else		
			response[:enrolled]=false
		end	
	else #In the case of an invalid request, send this JSON
		puts requestFromDevice
		response[:status]="fail"
		response[:enrolled]="false"
	end
	response.to_json
end

# GetNextCommand [POST REQUEST]
# NOTE: Case Sensitivity is Important
# Request Sample:
# {
# 	"uuid": (String)<device id>
# }
#
# Registered:
# True/false depending of whether the device with the request uuid is registered
# If not registered, returns command_exists: false
# If not registered, returns test_case: {}
#
# Command_exists:
# Returns true/false depending on whether there are commands on queue
# 
# Test_case:
# Returns {} if no tests lined up
#
# Steps:
# Array of steps for the device to follow (Should follow proper formating)
# 
# Response Sample:
# {
# 	"registered": (boolean)<true,false>,
# 	"command_exists": (boolean)<true,false>,
# 	"test_case": (JSON) {
# 		"steps": (Array<Strings>) [<steps 1 ... n>]
# 	}
# }

post '/GetNextCommand' do
	#Building the response hash that will be returned as a JSON
	response={}
	invalidRequest=false

	begin # In the case of an invalid request
		@requestFromDevice=JSON.parse(request.body.read)
		puts "Request JSON: " + @requestFromDevice.to_s
	rescue
		invalidRequest=true
	end
	
	if !invalidRequest
		#Begin processing the request
		if @requestFromDevice.is_a?(::Hash) && @requestFromDevice.has_key?("uuid") && !@requestFromDevice["uuid"].empty? # Request is valid
			if isRegistered(enrolled_devices, @requestFromDevice["uuid"])
				response[:registered]=true 
				# Check if the command is present
				puts "Checking"
				if !Dir.glob('*.json').empty?
					puts "Here"
					filename = Dir['commands-*.json'][0]
					puts "#{filename} is present"
					response[:command_exists]=true
					response[:test_case]=getTestCase(filename)
				else
					puts "Not Here"
					puts "commands.json is not present"
					response[:command_exists]=false
					response[:test_case]={}
				end
			else
				response[:registered]=false
				response[:command_exists]=false
				response[:test_case]={}
			end
		else # Request is invalid
			response[:registered]=false
			response[:command_exists]=false
			response[:test_case]={}
		end
	else
		response[:registered]=false
		response[:command_exists]=false
		response[:test_case]={}
	end

	puts "Response JSON: " + response.to_s
	response.to_json
	
end

get '/' do
	erb :form
end


# SetNextCommand [POST REQUEST]
# NOTE: Case Sensitivity is Important
# Request Sample:
# In the params, send in the value of the dropdown
# ie, Enable Bluetooth
# 
# Response Sample:
# {
# 	"status":(string)<"success","fail">
# }

post '/SetNextCommand' do
	File.open("commands-#{rand(1000)}.json",'w') do |file|
		puts params[:command]

		command = {}
		command[:steps] = [params[:command]]

		puts command.to_s
		file.puts command.to_json
	end
	redirect '/'
end
