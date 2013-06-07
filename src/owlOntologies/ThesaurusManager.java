package owlOntologies;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.HashMap;
import java.util.regex.*;

public class ThesaurusManager {
	
	
	private HashMap<String, HashSet<String>> thesaurus;
	
	
	//we expect noone to touch the thesaurus except us.  allow for initialization, then a checker and a getter.  dont allow a setter as we
	//want that all handeled by the file itself.  it we want new associations, then modify the file
	public ThesaurusManager(String filePath) throws IOException{
		thesaurus = generateThesauri(filePath);
	}
	
	
	public boolean hasKey(String key){
		return thesaurus.containsKey(key);
	}
	
	public HashSet<String> getSynonyms(String key){
		return thesaurus.get(key);
	}
	
	
	//this method is the manager that will read in the synonym list, and create a map between the head word, and a list of synonyms (though
	//for ease of access the list is stored as a set.
	//the file is set up like this;  headWord \t PoS \t listOfSynonyms (where each word in the list is deliniated by a ','
	//@param filePath, the file path that contains the synonym list
	public HashMap<String, HashSet<String>> generateThesauri(String filePath) throws IOException{
		BufferedReader in = new BufferedReader(new FileReader(new File(filePath)));
		
		HashMap<String, HashSet<String>> thesaurus = mapGenerator(in);
		
		return thesaurus;
		
	}
	
	//this method goes line by line through the synonym file, and creates the map (see generateThesauri)
	//@param in a buffered reader of the synonym file 
	public HashMap<String, HashSet<String>> mapGenerator(BufferedReader in) throws IOException{
		
		HashMap<String, HashSet<String>> thesaurus = new HashMap<String, HashSet<String>>();
		
		//for each line we read in split by tabs, the first is the head word, throw away the second, and then split the third by commas
		//to get each word.  make the headword the key in the hashmap and make a hashset out of the list, and make it the value
		String current = in.readLine();
		while (current != null){
			String[] result = current.split("\t");
			String key = result[0]; //headword
			
			String[] listOfWords = result[2].split(","); //list of synonyms
			HashSet<String> value = new HashSet<String>();
			for(int i =0; i < listOfWords.length; i++)
				value.add(listOfWords[i]);
			
			thesaurus.put(key, value);
			
			current = in.readLine();
		}
		return thesaurus;
	}
	

}
