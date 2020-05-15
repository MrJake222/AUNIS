package mrjake.aunis.loader.model;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

public class OBJModel {
	
	private int drawCount;
	private boolean modelInitialized;
	
	private int vId;
	private int tId;
	private int nId;
	private int iId;
	private boolean hasTex;
	
	private float[] vertices;
	private float[] textureCoords;
	private float[] normals;
	private int[] indices;
	
	
	public OBJModel(float[] vertices, float[] textureCoords, float[] normals, int[] indices, boolean hasTex) {
		this.vertices = vertices;
		this.textureCoords = textureCoords;
		this.normals = normals;
		this.indices = indices;
		this.hasTex = hasTex;

		modelInitialized = false;
	}
	
	public void initializeModel() {
		drawCount = indices.length;

		vId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vId);
		glBufferData(GL_ARRAY_BUFFER, createFloatBuffer(vertices), GL_STATIC_DRAW);
		
		if (hasTex) {
			tId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, tId);
			glBufferData(GL_ARRAY_BUFFER, createFloatBuffer(textureCoords), GL_STATIC_DRAW);
		}
		
		nId = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, nId);
		glBufferData(GL_ARRAY_BUFFER, createFloatBuffer(normals), GL_STATIC_DRAW);
		
		iId = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iId);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, createIntBuffer(indices), GL_STATIC_DRAW);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		modelInitialized = true;
	}
	
	public void render() {
		if (!modelInitialized)
			initializeModel();
		
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_NORMAL_ARRAY);
		if (hasTex) glEnableClientState(GL_TEXTURE_COORD_ARRAY);
		
		glBindBuffer(GL_ARRAY_BUFFER, vId);
		glVertexPointer(3, GL_FLOAT, 0, 0);
		
		if (hasTex) {
			glBindBuffer(GL_ARRAY_BUFFER, tId);
			glTexCoordPointer(2, GL_FLOAT, 0, 0);
		}
		
		glBindBuffer(GL_ARRAY_BUFFER, nId);
		glNormalPointer(GL_FLOAT, 0, 0);
		
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, iId);		
		glDrawElements(GL_TRIANGLES, drawCount, GL_UNSIGNED_INT, 0);
		
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
		
		glDisableClientState(GL_VERTEX_ARRAY);
		glDisableClientState(GL_NORMAL_ARRAY);
		if (hasTex) glDisableClientState(GL_TEXTURE_COORD_ARRAY);
	}
	
	private FloatBuffer createFloatBuffer(float[] input) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(input.length);
		buffer.put(input).flip();
		
		return buffer;
	}
	
	private IntBuffer createIntBuffer(int[] input) {
		IntBuffer buffer = BufferUtils.createIntBuffer(input.length);
		buffer.put(input).flip();
		
		return buffer;
	}
}
