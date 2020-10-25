#version 120

uniform sampler2D DiffuseSampler;

varying vec2 fragCoord1;
varying vec2 fragCoord2;
varying vec2 fragCoord3;
varying vec2 fragCoord4;
varying vec2 fragCoord5;

void main() {
    vec4 color = texture2D(DiffuseSampler, fragCoord1);
    color += texture2D(DiffuseSampler, fragCoord2);
    color += texture2D(DiffuseSampler, fragCoord3);
    color += texture2D(DiffuseSampler, fragCoord4);
    color += texture2D(DiffuseSampler, fragCoord5);

    gl_FragColor = color * 0.2;
}
