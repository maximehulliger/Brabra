============================================
=  ______           _                 _    =
=  | ___ \         | |               | |   =
=  | |_/ /_ __ __ _| |__  _ __ __ _  | |   =
=  | ___ \ '__/ _` | '_ \| '__/ _` | | |   =
=  | |_/ / | | (_| | |_) | | | (_| | |_|   =
=  \____/|_|  \__,_|_.__/|_|  \__,_| (_)   =
=                                          =
============================================


0.	Brabra !

This augmented reality project was initially developed for the course
CS-211 "Introduction to visual computing" given in 2014-2015 at EPFL.

>> Featuring:
- Processing 3, Java 8 and probably Scala soon
- 3D interactive physic simulation
- A starship shooting missiles
- Dynamic scene initialization
- Image analysis for quad detection (bugged for now)

for developers see also "readmeDev.md".

1.	Real Game

The key interface of the project :D

- q, r     ->  restart the game
- <tab>	   ->  change the camera mode

>> focused object
You can focus your interaction on an object or body. 

- e        ->  shoot the biggest ready missile (if available)
- w, s     ->  go forward / backward
- space   ->  unbrake (less brake)
- mouse drag, a, d
				->  turn the object around
- scroll wheel 	->  change the interaction force

>> Camera
The camera can work in several modes and follow a particular object.

>> Scene initialization
You will find an input file "Brabra/bin/input/scene.xml" to configure the initialization 
of the scene's objects and parameters (camera, physic). 

2. 	Trivial Game

Initial excepted project for the course. Only reacts to the rotation of the detected plate.

3.	Image Analysis (not working atm)

>> plate
The augmented reality aspect of this project remains in the possibility to control
the software with an external object. 
We want to detect a plate or quad with a particular colour. (example: see "plate exemple.jpg")
Once calibrated, the software will compute the rotation of the plate relative of the camera.
In option, it also detects two buttons on each site of the plate.

- rotation       ->  turn the focused object around
- left button	 ->  go forward, brake if not visible
- right button   ->  shoot missiles bigger with the visibility

>> calibration
Here you can find and manage image filtering parameters to operate the image analysis.
It is mainly thresholding of the pixel colours in both RGB and HSV representation.

- p	-> pause/play the input
- i -> change the input (camera / example video)
- l	-> load a parameters file
- s	-> save current parameters
- b -> go in 'button detection' mode, to calibrate the button detection

>> control screen
When image analysis is enabled and not paused, a control or feedback screen will be 
displayed in the upper left corner of the window reporting what the software sees.

- darker control screen -> no quad detected
- quad in red -> rotation of the plate is too big
			(max 65°, happens too when the quad is wrongly detected.)
- button circles are red -> not enough detected to count

4. 	Contact

Maxime Hulliger, hulliger@epfl.ch