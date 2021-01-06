#version 120

attribute vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform float multiplier;
uniform float baseOffset;

varying vec2 fragCoord1;
varying vec2 fragCoord2;
varying vec2 fragCoord3;
varying vec2 fragCoord4;
varying vec2 fragCoord5;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    vec2 uv = 1.0 / InSize;
    float offset = baseOffset * multiplier;

    fragCoord1 = Position.xy * uv;
    fragCoord2 = fragCoord1 + vec2(-offset, -offset) * uv;
    fragCoord3 = fragCoord1 + vec2(-offset, offset) * uv;
    fragCoord4 = fragCoord1 + vec2(offset, offset) * uv;
    fragCoord5 = fragCoord1 + vec2(offset, -offset) * uv;
}
