# Smooth Metal Map
* Forum: https://discord.gg/cE9ZmNGJSP
* Github: https://github.com/Wanucha/SmoothMetalMap
* Merges smoothness/roughness and metallic maps to single image, that can be used in Unity.
* Can be used only with GUI, command line is not implemented yet
## Function
* Takes average RGB from metallic map and stores to result red channel
* Takes average RGB from smoothness map and stores to alpha channel
* In case of roughness inverts alpha channel: a = 255 - a
* Result green and blue channels are 0
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