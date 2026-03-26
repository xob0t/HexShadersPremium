// vertex shader

uniform sampler2D u_RenderedTexture1;
uniform sampler2D u_RenderedTexture2;

uniform mediump float u_PointSize;
uniform mediump float u_Step;

attribute highp vec2 a_PointPosition;
attribute highp vec2 t_PointPosition;

varying lowp vec4 v_PointColor;

void main()
{
    v_PointColor = mix(
        texture2D(u_RenderedTexture1, t_PointPosition),
        texture2D(u_RenderedTexture2, t_PointPosition),
        u_Step);
        
	gl_Position.xy = a_PointPosition * 1.04;
	gl_Position.zw = vec2(1.0);
	gl_PointSize = u_PointSize;
}

====
// fragment shader

precision lowp float;

uniform sampler2D u_HexagonTexture;

varying vec4 v_PointColor;

void main()
{
    gl_FragColor = v_PointColor * texture2D(u_HexagonTexture, gl_PointCoord);
}