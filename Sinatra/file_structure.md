This app will follow a file structure similar to rails

├── Sinatra 
│   ├──app
│   │   ├──controller
│   │   ├──helpers
│   │   ├──views
│   ├──doc
│   ├──log
│   ├──lib
│   ├──test
│   ├──tmp

app : This organizes your application components. 

app/controllers: Where all the server requests logic is placed

app/helpers: Supporting logic for the controller goes here

app/views: HTML/ERB views go here.

doc: Documentation goes here

lib: You'll put libraries here, unless they explicitly belong elsewhere (such as vendor libraries).

log: Error logs go here. 

test: Location that stores .json files with valid JSON testcases. See the documentation for what a valid testcase JSON should look like

tmp: Holds temporary files for intermediate processing.
