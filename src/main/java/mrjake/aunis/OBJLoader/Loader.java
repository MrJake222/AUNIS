package mrjake.aunis.OBJLoader;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import mrjake.aunis.Aunis;

public class Loader {
	public Loader() {
		
	}
	
	public Model loadModel(String filename) {
		BufferedReader reader;
		
		List<Vector3f> vertices = new ArrayList<Vector3f>();
		List<Vector3f> normals = new ArrayList<Vector3f>();
		List<Vector2f> textures = new ArrayList<Vector2f>();
		List<Integer> indices = new ArrayList<Integer>();
	    //List<Vertex> vertexList = new ArrayList<Vertex>();
	    Map<Vertex, Integer> vertexIndexMap = new LinkedHashMap<Vertex, Integer>();
	    
	    int current = 0;
	    
		try {
			reader = new BufferedReader(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(filename), "UTF-8"));
		    String line;
		    
			while ( (line = reader.readLine()) != null ) {
				
				if (line.startsWith("v "))
					vertices.add( splitToVector(line) );
				
				else if (line.startsWith("vt "))
					textures.add( splitToVector2(line) );
	
				else if (line.startsWith("vn ")) 
					normals.add( splitToVector(line) );
				
				else if (line.startsWith("f ")) {
					String [] face = line.split(" ");
					
					for (String verticle : face) {
						if ( verticle.equals("f") ) continue;
						
						String[] vtn = verticle.split("/");
						Vertex vertex = new Vertex(vtn);
						
						Integer index = vertexIndexMap.get(vertex);
						
						if (index != null) {
							// Index found -> duplicate -> add index to list
							indices.add(index);
						}
						
						else {
							// No index found -> new one -> add to Map with <current> index
							vertexIndexMap.put(vertex, current);
							indices.add(current);		
							
							current++;
						}
					}
				}
			}
			
			reader.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		String[] path = filename.split("/");
		Aunis.logger.info(String.format("Finished loading mesh %s: v:%d t:%d n:%d vertexMap:%d indices:%d", path[path.length-1], vertices.size(), textures.size(), normals.size(), vertexIndexMap.size(), indices.size() ));

		// ----------------------------------------------------------------------------------
		
		int size = vertexIndexMap.size();
		int index = 0;
		
		float[] v = new float[ size * 3 ];
		float[] t = new float[ size * 2 ];
		float[] n = new float[ size * 3 ];
		
		for ( Vertex key : vertexIndexMap.keySet() ) {
			
			Vector3f ver = vertices.get( key.vId );
			Vector2f tex = textures.get( key.tId );
			Vector3f norm = normals.get( key.nId );
			
			v[ index*3    ] = ver.x;
			v[ index*3 + 1] = ver.y;
			v[ index*3 + 2] = ver.z;
			
			t[ index*2    ] = tex.x;
			t[ index*2 + 1] = -tex.y;
			
			n[ index*3    ] = norm.x;
			n[ index*3 + 1] = norm.y;
			n[ index*3 + 2] = norm.z;
			
			index++;
		}
		
		int[] ind = new int[ indices.size() ];
		
		for (int k=0; k<indices.size(); k++) {
			ind[k] = indices.get(k).intValue();
		}

		return new Model(v, t, n, ind);
	}
	
	class Vertex {
		int vId;
		int tId;
		int nId;
		
		public Vertex(int v, int t, int n) {
			vId = v;
			tId = t;
			nId = n;
		}
		
		public Vertex(String[] vtn) {
			if (vtn.length == 3) {
				vId = Integer.valueOf(vtn[0]) - 1;
				
				if ( !vtn[1].isEmpty() )
					tId = Integer.valueOf(vtn[1]) - 1;
				
				if ( !vtn[2].isEmpty() )
					nId = Integer.valueOf(vtn[2]) - 1;
			}
		}		
		
		public String toString() {
			return vId + "/" + tId + "/" + nId;
		}

		private Loader getOuterType() {
			return Loader.this;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + nId;
			result = prime * result + tId;
			result = prime * result + vId;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Vertex other = (Vertex) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (nId != other.nId)
				return false;
			if (tId != other.tId)
				return false;
			if (vId != other.vId)
				return false;
			return true;
		}
	}
	
	private Vector3f splitToVector(String line) {
		String [] splitted = line.split(" ");
		
		float x = Float.valueOf( splitted[1] );
		float y = Float.valueOf( splitted[2] );
		float z = Float.valueOf( splitted[3] );
		
		return new Vector3f(x,y,z);
	}
	
	private Vector2f splitToVector2(String line) {
		String [] splitted = line.split(" ");
		
		float x = Float.valueOf( splitted[1] );
		float y = Float.valueOf( splitted[2] );
		
		return new Vector2f(x,y);
	}
}
