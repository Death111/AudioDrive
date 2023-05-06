# AudioDrive
AudioDrive is a project done for a "advanced game development" lecture. It was developed by two students. The game is built using Java with LWJGL. The gameplay is similar to AudioSurf, where you choose an audio track, and the game generates a map for you to play. It was developed in 2014.

![screenshot.jpg](/readme/screenshot.jpg)

## How to Play
To play AudioDrive, you first need to choose an audio track. Once you have selected your desired audio track, the game will generate a map based on the audio's waveform. The objective of the game is to navigate through the generated map while avoiding obstacles and collecting power-ups.  
[![Check it out on youtube](https://img.youtube.com/vi/G9f1wQDQ-gI/0.jpg)](https://www.youtube.com/watch?v=G9f1wQDQ-gI)


The game features simple controls, allowing players to move left or right to avoid obstacles and collect power-ups. The game also includes a scoring system that keeps track of your progress throughout the game.

## How to Run
To run AudioDrive, you first need to have Java 11 installed on your windows computer (we added only the windows natives).
After downloading the repository, you can run the game by opening the project in your preferred Java IDE and running the main method. 

## Features
- Reflections
- Glow effect
- 2D + 3D particles
- Music analysis and level generation
- Camera animation support from Adobe After Effects
- Own .obj loader with .mtl file support

## Info
- We did remove some files in the git history for copyright reasons. This means that there will be files missing (mostly sound files) if you checkout an earlier version of the repository.
- The GC will create some lags while playing. We think that one improvement would be to create a pool for commonly used objects like vectors.