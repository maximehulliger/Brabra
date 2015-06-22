============================================
-  ______           _                 _    -
-  | ___ \         | |               | |   -
-  | |_/ /_ __ __ _| |__  _ __ __ _  | |   -
-  | ___ \ '__/ _` | '_ \| '__/ _` | | |   -
-  | |_/ / | | (_| | |_) | | | (_| | |_|   -
-  \____/|_|  \__,_|_.__/|_|  \__,_| (_)   -
-                                          -
============================================



 	Brabra !

ce projet de réalité augmentée a été développé dans le cadre du cours CS-211 
"introduction à l'informatique visuelle" donné en 2014-2015 à l'EPFL.



1.	Real Game

Jeu 3D développé avec passion. vous contrôlez un vaisseau spacial qui doit ... détruire des objectifs jaune.
Il est possible de paramètrer son armement à partir du menu (à venir). Plus une arme prend de place, plus elle est améliorée
le niveau d'une arme caractérise sa puissance. Plus une arme est puissante, plus elle prend de temps à recharger. 


	commande plaque:
- bouton gauche	 ->  fait avancer le vaisseau. freine si le bouton n'est pas visible
- bouton droit   ->  fait tirer les armes jusqu'à un niveau proportionnel à la visibilité
- rotation       ->  fait tourner le vaisseau sur lui même

	commandes clavier:
- q        ->  réinitialise le jeu
- e        ->  tire le plus important des missiles prêts
- w/s      ->  avance / recule
- espace   ->  debraie (moins de frein)
- mouse drag, ad 
           ->  fais tourner le vaisseau sur lui-même
- <tab>	   ->  change de vue
- mollette ->  change la puissance du vaisseau



2.	Calibration et écran de contrôle

- p	-> pause/play l'input
- i 	-> change d'input
- l	-> charge un fichier de paramètres
- s	-> sauve les parametres actuels de la camera
- b 	-> passe en mode 'button detection' : permet de calibrer la detection des boutons


Un écran de contrôle est affiché lors d'une partie dans le coin supérieur gauche.
- écran de contrôle plus foncé -> pas de plaque détectée.
- la plaque est affiché en rouge -> l'angle de la plaque trop grand 
			(max 65°, survient surtout quand la plaque est mal détectée.)
- les cercles des boutons sont rouges -> pas assez détecté pour compter.
