#version 120

uniform sampler2D DiffuseSampler;

varying vec2 texCoord;
varying vec2 oneTexel;

uniform float width;
uniform float outlineAlpha;
uniform float filledAlpha;

void main(){
    int intWidth = int(width);
    vec4 center = texture2D(DiffuseSampler, texCoord);

    if (center.a == 0.0) {
        // Scan pixels nearby
        for (int sampleX = -intWidth; sampleX <= intWidth; sampleX++) {
            for (int sampleY = -intWidth; sampleY <= intWidth; sampleY++) {
                vec2 sampleCoord = vec2(float(sampleX), float(sampleY)) * oneTexel;
                vec4 sampleColor = texture2D(DiffuseSampler, texCoord + sampleCoord);
                if (sampleColor.a > 0.0) {
                    // If we find a pixel that isn't transparent, set the frag color to it and replace the alpha.
                    center = vec4(sampleColor.rgb, outlineAlpha);
                }
            }
        }
    } else {
        // Replace filled alpha
        center = vec4(center.rgb, filledAlpha);
    }

    gl_FragColor = center;
}
