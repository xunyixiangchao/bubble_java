package com.lis.bubble_java.rendering;

/**
 * Created by lis on 2017/4/20.
 */

public class BubbleShader {

	public static String U_TEXT = "u_Text";
	public static String U_MATRIX = "u_Matrix";
	public static String U_BACKGROUND = "u_Background";
	public static String A_POSITION = "a_Position";
	public static String A_UV = "a_UV";

	// language=GLSL
	public static String vertexShader = "uniform mat4 u_Matrix;" +
	                                    "attribute vec4 a_Position;" +
	                                    "attribute vec2 a_UV;" +
	                                    "varying vec2 v_UV;" +
	                                    "void main()" +
	                                    "{" +
	                                    "gl_Position = u_Matrix * a_Position;" +
	                                    "v_UV = a_UV;" +
	                                    "}";

	// language=GLSL
	public static String fragmentShader = "precision mediump float;" +
	                                      "uniform vec4 u_Background;" +
	                                      "uniform sampler2D u_Texture;" +
	                                      "varying vec2 v_UV;" +
	                                      "void main()" +
	                                      "{" +
	                                      "float distance = distance(vec2(0.5, 0.5), v_UV);" +
	                                      "gl_FragColor = mix(texture2D(u_Texture, v_UV), u_Background, smoothstep(0.49, 0.5, distance));" +
	                                      "}";
}
