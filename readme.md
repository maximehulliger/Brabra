```sh

============================================  
=  ______           _                 _    =  
=  | ___ \         | |               | |   =  
=  | |_/ /_ __ __ _| |__  _ __ __ _  | |   =  
=  | ___ \ '__/ _` | '_ \| '__/ _` | | |   =  
=  | |_/ / | | (_| | |_) | | | (_| | |_|   =  
=  \____/|_|  \__,_|_.__/|_|  \__,_| (_)   =  
=                                          =  
============================================  
```


#	Brabra !
This augmented reality project was initially developed for the course
CS-211 "Introduction to visual computing" given in 2014-2015 at EPFL.

###### Featuring:
- Processing 3, Java 8, Java FX and ready for Scala
- 3D interactive physic simulation
- A starship shooting missiles
- Dynamic scene initialization
- Image analysis for quad detection (bugged for now)

*For developers, see also the [readmeDev](readmeDev.md) file.*

<br>

# 1.	Real Game
The key interface of the project :D

- *q*, *r*     ->  restart the game
- <_tab_>	   ->  change the camera mode

<br>

#### Focused object
You can focus your interaction on an object or body. 

- *e*        ->  shoot the biggest ready missile (if available)
- *w, s*     ->  go forward / backward
- space   ->  unbrake (less brake)
- mouse drag, *a*, *d*
				->  turn the object around
- scroll wheel 	->  change the interaction force

<br>

#### Camera
The camera can work in several modes and follow a particular object.

modes: ***fixed, static, relative***.

each mode has his own distance from the looked point.

<br>

#### Scene initialization
You will find an input file "Brabra/bin/input/scene.xml" to configure the initialization 
of the scene's objects and parameters (camera, physic). To reload the file, restart the game with *q* or *r*.

supported object names: ***floor, ball, box, objectif, starship***.

supported attributes: ***pos, dir, mass, name, impluse, [color, stroke], life, camera, cameraDist, focus***.

supported parameters: <i>**physic**: gravity, deltaTime. **camera**: displaySkybox, displayAxis, [mode, dist]</i>

file example with all supported attributes:

	<?xml version="1.0" encoding="UTF-8"?>
	<scene>
		<camera displaySkybox="true" mode="not" dist="(200,200,200)"></camera>
		<physic gravity="0.2" deltaTime="0.7"></physic>
		<floor pos="zero" color="grass" stroke="green"></floor>
		<ball pos="(0,5,5)" mass="5"></ball>
		<box pos="(5,5,0)" name="box1"></box>
		<objectif pos="(100,100,-500)" life="200" color="red"></objectif>
		
		<starship pos="(100,300,100)" dir="left" camera="relative" focus="true" force="40"></starship>
		
		<!--  
		<objectif pos="(1000,100,-2000)" life="50/200" color="red"></objectif>
		-->
	</scene>

<br>

# 2. 	Trivial Game
Initial excepted project for the course. Only reacts to the rotation of the detected plate.

<br>

# 3.	Image Analysis (not working atm)
#### Plate
The augmented reality aspect of this project remains in the possibility to control
the software with an external object. 
We want to detect a plate or quad with a particular colour (example: see "plate exemple.jpg"). Once calibrated, the software will compute the rotation of the plate relative of the camera.
In option, it also detects two buttons on each side of the plate.

- rotation       ->  turn the focused object around
- left button	 ->  go forward, brake if not visible
- right button   ->  shoot missiles bigger with the visibility

<br>

#### Calibration
Here you can find and manage image filtering parameters to operate the image analysis.
It is mainly thresholding of the pixel colours in both RGB and HSV representation.

- p	-> pause/play the input
- i -> change the input (camera / example video)
- l	-> load a parameters file
- s	-> save current parameters
- b -> go in 'button detection' mode, to calibrate the button detection

<br>

#### Control screen
When image analysis is enabled and not paused, a control or feedback screen will be 
displayed in the upper left corner of the window reporting what the software sees.

- darker control screen -> no quad detected
- quad in red -> rotation of the plate is too big
			(max 65°, happens too when the quad is wrongly detected.)
- button circles are red -> not enough detected to count

<br>

# 4. 	Contact

<br>

Maxime Hulliger, hulliger@epfl.ch
