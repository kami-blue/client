#version 120

attribute vec4 Position;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;
uniform vec2 randomSeed;
uniform float radius;

varying vec2 fragCoord;
varying vec2 uv;
varying vec2 offset;

void main(){
    vec4 outPos = ProjMat * vec4(Position.xy, 0.0, 1.0);
    gl_Position = vec4(outPos.xy, 0.2, 1.0);

    fragCoord = Position.xy / OutSize;
    fragCoord.y = 1.0 - fragCoord.y;

    uv = 1.0 / InSize * radius;
    offset = vec2(sin(dot(fragCoord, randomSeed)), sin(dot(vec2(fragCoord.y, fragCoord.x), randomSeed)));
}
