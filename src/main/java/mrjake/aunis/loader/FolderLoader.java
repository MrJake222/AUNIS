package mrjake.aunis.loader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import mrjake.aunis.Aunis;

public class FolderLoader {
	
	public static List<String> getAllFiles(String path, String... suffixes) {		
		List<String> out = new ArrayList<>();
		
		InputStream stream = Aunis.instance.getClass().getClassLoader().getResourceAsStream(path);
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        
        try {
        	while (reader.ready()) {
        		String name = path + "/" + reader.readLine();
        		
        		boolean endsWith = false;
        		for (String suffix : suffixes) {
					if (name.endsWith(suffix)) {
						endsWith = true;
						break;
					}
				}
        		
        		if (endsWith)
        			out.add(name);
        		else
        			out.addAll(getAllFiles(name, suffixes));
        	}
        }
        
        catch (IOException e) {
        	e.printStackTrace();
        }
        
        finally {
        	try {
				reader.close();
			} catch (IOException e) {}
        }
        
        return out;
	}
}
