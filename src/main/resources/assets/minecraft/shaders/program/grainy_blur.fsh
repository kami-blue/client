#version 120

uniform sampler2D DiffuseSampler;

uniform vec2 randomSeed;
uniform float iterations;

varying vec2 fragCoord;
varying vec2 uv;
varying vec2 offset;

void main() {
    int intIterations = int(iterations);
    vec2 randomOffset = offset;
    vec4 color = texture2D(DiffuseSampler, fragCoord + randomOffset * uv);

    for (int i = 1; i < intIterations; i++) {
        color += texture2D(DiffuseSampler, fragCoord + randomOffset * uv);
        randomOffset = vec2(sin(dot(randomOffset, randomSeed)), sin(dot(vec2(randomOffset.y, randomOffset.x), randomSeed)));
    }

    gl_FragColor = color / float(intIterations);
}
