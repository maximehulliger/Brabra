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

ce projet de r�alit� augment�e a �t� d�velopp� dans le cadre du cours CS-211 
"introduction � l'informatique visuelle" donn� en 2014-2015 � l'EPFL.



1.	Real Game

Jeu 3D d�velopp� avec passion. vous contr�lez un vaisseau spacial qui doit ... d�truire des objectifs jaune.
Il est possible de param�trer son armement � partir du menu (� venir). Plus une arme prend de place, plus elle est am�lior�e
le niveau d'une arme caract�rise sa puissance. Plus une arme est puissante, plus elle prend de temps � recharger. 


	commande plaque:
- bouton gauche	 ->  fait avancer le vaisseau. freine si le bouton n'est pas visible
- bouton droit   ->  fait tirer les armes jusqu'� un niveau proportionnel � la visibilit�
- rotation       ->  fait tourner le vaisseau sur lui m�me

	commandes clavier:
- q        ->  r�initialise le jeu
- e        ->  tire le plus important des missiles pr�ts
- w/s      ->  avance / recule
- espace   ->  debraie (moins de frein)
- mouse drag, ad 
           ->  fais tourner le vaisseau sur lui-m�me
- <tab>	   ->  change de vue
- mollette ->  change la puissance du vaisseau



2.	Calibration et �cran de contr�le

- p	-> pause/play l'input
- i 	-> change d'input
- l	-> charge un fichier de param�tres
- s	-> sauve les parametres actuels de la camera
- b 	-> passe en mode 'button detection' : permet de calibrer la detection des boutons


Un �cran de contr�le est affich� lors d'une partie dans le coin sup�rieur gauche.
- �cran de contr�le plus fonc� -> pas de plaque d�tect�e.
- la plaque est affich� en rouge -> l'angle de la plaque trop grand 
			(max 65�, survient surtout quand la plaque est mal d�tect�e.)
- les cercles des boutons sont rouges -> pas assez d�tect� pour compter.
