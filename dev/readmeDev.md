# Installation
Those are the steps to get you started with the project in Eclipse.

#### 1.1 IDE setup 
Get eclipse IDE (for scala :D )
	
	http://scala-ide.org/download/sdk.html

Or update current eclipse for scala (by drag & drop into eclipse)
	
	http://scala-ide.org/download/current.html

We don't use scala for now but it's a really powerful language built on top of java, totally compatible with it. We might want to use it later (You will too ;) ). 

#### 1.2 Get the project files
Create a folder for the project, recommended in

	%name%/workspace/Java/Brabra
	
Get it from github (account needed)

	https://github.com/maximehulliger/Brabra

Click on "save max.. ..Github desktop" button, left from "Download ZIP". ***Don't forget to star it !*** (top right).

#### 1.3 Import the Brabra project
Now we'll create the eclipse project to run it. File -> Import... -> Existing project into workspace -> browse to the Brabra folder -> Finish.

#### 1.4 Activate JUnit in Eclipse
In package explorer, right click on the Brabra folder, then Build Path / Add Libraries... -> JUnit -> Next -> Finish.

#### 1.5 Add external libraries
Add the processing & co libraries to the build path: right click on the libs (or folder) -> add to Build Path.

#### 1.6 Create run configurations
You need a run configuration to run some files in a certain way. You will need at least one main and one for the tests.

Click on the arrow next to the green play button -> Run Configurations... -> double-click on "Java Application".

> Main -> Name: `Brabra !`
>
> Main -> Main Class: `brabra.Brabra`
>
> Arguments -> VM arguments: `-ea`
>
> Environment -> New... -> Name: `export`, Value: `false`

`-ea` enable the use of 

	assert( 1 == 1 );
	
which is run only in eclipse, not in the final application.

##### 1.6 Play !
You should now be able to run the program by running the configuration :D

<br>

# Extra Installation (optionnal)
### To work with the server (Tomcat 8)
##### Install the tomcat runtime from:

	http://tomcat.apache.org/download-80.cgi#8.0.33

##### Install web plugins for Eclipse:
Help -> Install New Software...

Look for `developer tools` at `--All Available Sites--`.

Install `Eclipse Java EE..`, `Eclipse Java Web..`, `Eclipse Web..`.

Look for `JST server` and install everything (3).

##### Create a local Tomcat server:
Window -> Preferences -> >Server -> Runtime Environment -> Apache Tomcat v8 -> [create new server: yes] -> next -> [tomcat installation dir: ...] -> Finish.

##### Import the nested Server project:
Right click in Package Explorer -> Import... -> Existing Project Into Workspace -> [root folder] -> [Search for nested project: yes] -> Server (Brabra should also appear) -> Next...

##### Configure the Server project for web development:
Right click on Server project -> Propreties -> Facets -> [+Dynamic Web Model +Java, choose the last version] -> Apply -> Ok.

##### Link server project to Brabra project
Right clic on Server project -> Preferences -> Java Build Path -> Project -> Add -> Brabra -> OK -> Apply

##### OK !
You should see no warnings or exclamation points on the projects. 

#### Export
If you want to export the application over all platforms (Windows, Linux, M.), you can install the Proclipsing plugin for Eclipse.

#### Git plugin
You can instal some git plugins for Eclipse to see the git state of the files.

Help -> Install New Software...

> Work with: `--- All Available Sites ---`, Filter text: `git`

Everything under collaboration should do the trick.

<br>

# Project structure
## Files
#### src/
`brabra/` contains all source files.

`data/` contains all resources (img, model, etc).

`input/` contains all input files that can be modified by the user.

#### lib/
Contains the external libraries used by the project. 
`core/` and `video` are for Processing.

`papaya` is for matrix manipulation;

#### dev/
Contains all the external files for developers.

#### tests/
yeah, really ;)

## Packages
> Notable feature implemented here.

### brabra
#### gui
> ToolWindow is the JavaFX thread.

#### calibration
#### imgprocessing
#### trivial
#### game
> Simulation interface.
> Scene file loading.

##### physic
> Physic magic.

###### geo
> Geometric shapes & Quaternion.

##### scene
> Objects.

###### fun
> Starship

###### weapons
> Weaponry & explosions.

<br>

# To read
lambda functions where added with Java 8. It's nothing compared to Scala but still a feature to know :)

##### java 8: lambda interface
http://www.java2s.com/Tutorials/Java/Java_Lambda/0140__Java_Instance_Method_Reference.htm

<br>

# Troubleshooting
##### General for Eclipse
You can refresh the files in the package explorer (with right click or f5).

If things are still weird after having restarted Eclipse, you can try to clean the project in Project -> Clean...

##### java FX: Api restriction bug
Just a bug I encountered with JavaFX. you may not.

http://stackoverflow.com/questions/25222811/access-restriction-the-type-application-is-not-api-restriction-on-required-l