# Smooth Metal Map
Merges smoothness/roughness and metallic maps to single image, that can be used in Unity.
## Function
* Takes average RGB from metallic map and stores to result red channel
* Takes average RGB from smoothness map and stores to alpha channel
* In case of roughness, recalculates: alpha = 255 - alpha
* Result green and blue channels are 0
## Usage
1. Drag metallic map to the left panel
2. Drag smoothness or roughness map to the middle panel **(both images must have same resolution)**
3. If you have smoothness map, uncheck 'invert', if you have roughness, check 'invert'
4. Click generate
5. Save the result file