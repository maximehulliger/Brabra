#	Brabra !
This augmented reality project was initially developed for the course
CS-211 "Introduction to visual computing" given in 2014-2015 at EPFL.
An interaction window was added for the course 
DH2642 "Interaction Programming and the Dynamic Web" given in 2015-2016 at KTH.

###### Featuring:
- Processing 3, Java 8, JavaFX
- 3D dynamic physic simulation with ODE
- Dynamic scene initialization from xml file and server
- Starships shooting missiles
- A GUI tool window
- Image analysis for quad detection

*For developers, see also the [readmeDev](dev/readmeDev.md), [todo](dev/todo.md) and [todoJavaFX](dev/todoJavaFX.md) files.*

Content: 
- 1. [Real Game](#real_game)
- 2. [Tool Window](#tool_window)
- 3. [Trivial Game](#trivial_game)
- 4. [Image Analysis](#image_analysis)
- 5. [Contacts](#contacts)

*****

<a id="real_game"></a>

# 1. Real Game			

The key interface of the project :D

It display the view of the scene from a camera. The scene contains all the simulated objects. 

- `q` =>  restart the game
- `<tab>`	   	=>  change the camera mode

<br>

#### Scene
The existing objects are separated in 4 main category: 
- Body (Object)
- Effect (Object)
- Weaponry, weapons (Object)
- Camera (Object)

<br>

#### Camera
The camera is an object that carry the camera in the scene.
It can work in several modes and follow a particular object (focused).

- **relative**: look from the parent perspective (follow his rotation too).
- **static**: look at the location of the focused.
- **none**: look at zero.

Each mode has his own distance from the focused (looked) point.

press `<tab>` to change the camera mode.

<br>

#### Focused object/body
You can focus your interaction on an object or body. 

- `e`       	=>  shoot the biggest ready missile (if available)
- `w`, `s` 		=>  go forward / backward
- `<alt>`		=>	brake
- `<space>`   	=>  unbrake (less brake)
- `<mouse drag>`, `a`, `d`
						=>  turn the object around
- `<scroll wheel>` 	=>  change the interaction force

<br>

#### Scene initialization
You will find an input file `Brabra/bin/input/scene.xml` to configure the initialization 
of the scene's objects and parameters: camera and physic(in settings). To reload the file, restart the game with `q` or `r`.

supported parameters: <i>**settings**: gravity, running, verbosity. **camera**: displaySkybox, displayAxis, displayCenterPoint, [mode, dist]</i>

supported object names: ***object, movable, plane, ball, box, starship :rocket:, target***.

supported object attributes: ***pos, parency, name, life, [color, (stroke)], [camera, (cameraDist)], displayCollider, debug***.

supported body attributes: ***size, mass, name, displayCollider, velocity, rotationVel, [focus, (force)]***.

supported object names: ***Floor, Plane, Ball, Box, Starship, Target***.

supported weaponry attributes: <i>**weaponry**: displayColliders, puissance. **weapon**: tier, upgradeRatio, puissanceRatio, displayColliders</i>

supported weapon names: ***missile_launcher***

file example with all supported attributes:
	
	<?xml version="1.0" encoding="UTF-8"?>
	<Scene>
		<Settings gravity="0.9" running="true" verbosity="max" displayAllColliders="false"></Settings>
		
		<Camera displaySkybox="true" mode="not" dist="(300,300,300)" debug="false"></Camera>
		
		<Floor pos="zero" color="grass"></Floor>
		
		<Ball pos="(20,200,-300)" mass="5" color="red"></Ball>
		
		<Starship pos="(0,200,0)" focus="true" force="72" camera="static" debug="false" displayCollider="false">
			<Weaponry puissance="400" prefab="none" displayColliders="false">
				<missile_launcher pos="(30,-10,0)" tier="2" upgrade="1.2"></missile_launcher>
				<missile_launcher pos="(0,-15,0)" tier="3" upgrade="1.2"></missile_launcher>
				<missile_launcher pos="(-30,-10,0)" tier="2" upgrade="0.8" displayColliders="true"></missile_launcher>
			</Weaponry>
		</Starship>
		
		<!-- targets can be destroyed by missiles. -->
		<Target pos="(-200,200,0)" life="50/200" color="red"></Target>
		
	</Scene>
	
*****

<a name="tool_window"></a>

# 2. 	Tool Window
Created at KTH. It contains 6 views in 5 tabs, in one window (in his own javaFX thread): 

- 1. **Scene View**: Display the state of all the objects in the scene. It is possible to change their attributes.
- 2. **Parameters View**: Interface to set all static parameters (physic, camera, scene store address). 
- 3. **Object Selection View**: List of all the objects that can be created and added to the scene. Clicking on one lead to 4.
- 4. **Object Creation View**: Interface to create a specific object. Top-left button lead back to 3, Top-middle button add the object to the scene and Top-right button will be to drag & drop it into the scene (not yet implemented).
- 5. **My Scenes View**: Lists all the scenes available (locally). Left button loads the scene in the processing thread and right button opens it in an external editor. 
- 6. **Scene Store View**: Accesses a database (specified in the parameters) and display all the available scenes to download (with a name filter). Left button is to download it and right button is (once downloaded) to open it in 5.

All the fields will are generic and are whether closed (just display the value) or open (content is editable). There is a jersey server project in server/. 

*****

<a name="trivial_game"></a>

# 3. 	Trivial Game	
Initial excepted project for the course. Only reacts to the rotation of the detected plate.

*****

<a name="image_analysis"></a>

# 4.	Image Analysis
#### Plate
The augmented reality aspect of this project remains in the possibility to control
the software with an external object. 
We want to detect a plate or quad with a particular colour (example: see "plate exemple.jpg"). Once calibrated, the software will compute the rotation of the plate relative to the camera.
In option, it also detects two buttons on each side of the plate.

- `<rotation>`      =>  turn the focused object around
- `<left button>`	 =>  go forward, brake if not visible
- `<right button>`   =>  shoot missiles bigger with the visibility

example plate:

![Example plate with 2 buttons](plate_example.jpg)

#### Calibration
Here you can find and manage image filtering parameters to operate the image analysis.
It is mainly thresholding of the pixel colours in both RGB and HSV representation.

- `p` => pause/play the input
- `i` => change the input (camera / example video)
- `l` => load a parameters file
- `s` => save current parameters
- `b` => go in 'button detection' mode, to calibrate the button detection

<br>

#### Control screen
When image analysis is enabled and not paused, a control or feedback screen will be 
displayed in the upper left corner of the window reporting what the software sees.

- darker control screen => no quad detected
- quad in red => rotation of the plate is too big
			(max 65°, happens too when the quad is wrongly detected.)
- button circles are red => not enough detected to count

*****

<a name="contacts"></a>

# 5. 	Contacts
######Curator

Maxime Roger Baudouin Hulliger, `maxime.hulliger@epfl.ch`

######Contributors

Matthieu Pierre Girard, `matthieu.girard@epfl.ch`

Boris Adrien Zbinden, `boris.zbinden@epfl.ch`

Max Turpenteim, `maxtu@kth.se`

Kei Wakabayashi `keiw@kth.se`