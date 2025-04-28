# Smooth Metal Map
* Forum: https://discord.gg/cE9ZmNGJSP
* Github: https://github.com/Wanucha/SmoothMetalMap
* Merges smoothness/roughness and metallic maps to single image, that can be used in Unity.
* Can be used only with GUI, command line is not implemented yet
## Program arguments
* (optional) Single file with settings in yml format - will load the settings on start 
## Function
### Simple mapping (preset for Unity)
* Takes average RGB from metallic map and stores to result red channel
* Takes average RGB from smoothness map and stores to alpha channel
* In case of roughness inverts alpha channel: a = 255 - a
* Option to clamp alpha to 1..255 to prevent color loss on alpha 0
* Result green and blue channels are 0
### Advanced mapping
* Uncheck preset for Unity
* Now you can define each target channel from source textures
* Channel definition format:
  * Either a number 0..255
  * Or takes values from source textures, e.g. R1G1-B1 (averages channels from first texture, blue is inverted)
  * You can define one or more channels
  * Each channel is R/G/B/A followed by texture number 1/2
  * Each channel can be inverted with -
  * Examples:
    * 0 - sets value to 0
    * 255 - sets value to 255
    * A2 - takes alpha from second texture
    * -R1B2 - averages texture 1 inverted red and texture 2 blue channels 
## Usage
1. Drag metallic map to the left panel
2. Drag smoothness or roughness map to the middle panel **(both images must have same resolution)**
3. If you have smoothness map, uncheck 'invert', if you have roughness, check 'invert'
4. If you are missing one of the maps, you can generate it
   1. The first map must be loaded
   2. Click Generate missing
   3. Choose intensity
   4. Generate the missing map, the dimensions will be same as the other map
5. Click generate
6. Save the result file
