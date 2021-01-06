package mrjake.aunis.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import mrjake.aunis.Aunis;

public class FolderLoader {
	
	public static List<String> getAllFiles(String path, String... suffixes) throws IOException {		
		List<String> out = new ArrayList<>();
		
		String classPath = Aunis.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		Aunis.info(String.format("classPath was '%s'.", classPath));
		
		classPath = classPath.replaceAll("%20", " ");
		
		Aunis.info(String.format("classPath is  '%s'.", classPath));
		
		int separatorIndex = classPath.indexOf("!");
				
		// Separator found, we're inside a JAR file.
		if (separatorIndex != -1) {
			classPath = classPath.substring(5, separatorIndex);
			
			JarFile jar = new JarFile(classPath);				
		    Enumeration<JarEntry> entries = jar.entries();
		    
		    while (entries.hasMoreElements()) {
		    	String name = entries.nextElement().getName();
		  
		    	if (name.startsWith(path) && endsWith(name, suffixes)) {
		    		out.add(name);
				}
		    }
	    
	    	jar.close();
		}
		
		// No separator, it's a debug environment.
		else {
			getAllFilesDev(path, out, suffixes);
		}
  
		return out;
	}
	
	private static boolean endsWith(String in, String... suffixes) {
		for (String suffix : suffixes) {
			if (in.endsWith(suffix)) {
				return true;
			}
		}
		
		return false;
	}
	
	private static void getAllFilesDev(String path, List<String> out, String... suffixes) throws IOException {
		InputStream stream = Aunis.class.getClassLoader().getResourceAsStream(path);
		BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

		while (reader.ready()) {
			String name = path + "/" + reader.readLine();
			
			if (endsWith(name, suffixes))
				out.add(name);
			else
				getAllFilesDev(name, out, suffixes);
		}
		
		reader.close();
	}
}
