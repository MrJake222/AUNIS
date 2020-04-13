package mrjake.aunis.raycaster.util;

import java.util.Arrays;
import java.util.List;

import mrjake.vector.Vector2f;

public class Ray {
	// Direction and offset of the linear function coming through first and last vertex
	public float a;
	public float b;
		
	// List of vertices the ray consists of, from outer to inner
	public List<Vector2f> vertices;
	public int verticalOffset = 0;
	
	public Ray ( List<Vector2f> verts ) {
		this.vertices = verts;
		
		setLinear();
	}
	
	public Ray(Vector2f vert, Vector2f vert2) {
		vertices = Arrays.asList(vert, vert2);
		
		setLinear();
	}

	public Vector2f getVert( int index ) {
		return vertices.get(index);
	}	
	
	public Vector2f getVertWithOffset( int index ) {
		return vertices.get(index + verticalOffset);
	}	

	public void setVerticalOffset(int offset) {
		if (getVertCount() < 3)
			this.verticalOffset = 0;
		else
			this.verticalOffset = offset;
				
		setLinear();
	}
	
	public Vector2f getIntersect(float a2, float b2) {
		float x = (b2 - b) / (a - a2);
		float y = x*a + b;
		
		return new Vector2f(x,y);
	}

	@Override
	public String toString() {
		return String.format("y=%fx+%f", a, b);
	}
	
	public int getVertCount() {
		return vertices.size();
	}
	
	private void setLinear() {
		Vector2f func = linearFunction(vertices.get(0+verticalOffset), vertices.get(1+verticalOffset));

		this.a = func.x;
		this.b = func.y;
	}
	
	private Vector2f linearFunction(Vector2f A, Vector2f B) {
		float a = (B.y - A.y) / (B.x - A.x);
		float b = A.y - (a * A.x);
		
		return new Vector2f(a,b);
	}
}
