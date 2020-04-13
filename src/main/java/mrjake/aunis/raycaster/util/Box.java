package mrjake.aunis.raycaster.util;

import java.util.Arrays;
import java.util.List;

import mrjake.vector.Vector2f;

public class Box {
	private List<Ray> rays;
	private int verticalIndex;
	
	public Box(Ray ray, Ray ray2, Ray ray3, Ray ray4, int index) {
		this.rays = Arrays.asList(ray, ray2, ray3, ray4);
		this.verticalIndex = index;
		
//		for (Ray ray5 : rays) {
//			Aunis.info(ray5.toString());
//		}
	}
	
	public Box(List<Ray> rays) {
		this.rays = rays;
		this.verticalIndex = 0;
	}

	public boolean checkForPointInBox(Vector2f p) {
		int intersects = 0; 
		
		float a = 0.3f;
		float b = p.y - (a*p.x);
		
		for (int i=0; i<rays.size(); i++) {
			Ray currentRay = rays.get(i);
			currentRay.setVerticalOffset(verticalIndex);
						
			Vector2f inter = currentRay.getIntersect(a, b);
			
			if (inter.x > p.x) {
				float x0 = currentRay.getVertWithOffset(0).x;
				float x1 = currentRay.getVertWithOffset(1).x;
				
				float y0 = currentRay.getVertWithOffset(0).y;
				float y1 = currentRay.getVertWithOffset(1).y;
				
				float xMax = Math.max(x0, x1);
				float xMin = Math.min(x0, x1);
				
				float yMax = Math.max(y0, y1);
				float yMin = Math.min(y0, y1);
				
				if ( inter.x > xMin && inter.x < xMax && inter.y > yMin && inter.y < yMax ) {
					intersects++;
				}
			}
			
		}
		
//		if (intersects%2 > 0) {
//			Aunis.info(String.format("(%f,%f)", p.x, p.y));
//			Aunis.info(String.format("y=%fx+%f", a, b));
//			
//			for (Ray ray : rays)
//				Aunis.info(ray.toString());
//		}
		
		return (intersects%2 > 0);		
	}
}
