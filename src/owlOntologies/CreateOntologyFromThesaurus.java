package owlOntologies;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;


public class CreateOntologyFromThesaurus {
	
	
	/*
	 * This class is designed to read in a normalized corpus, and then using an thesaurus, generate an Owl ontology that represents that corpus.  it does this by
	 * saying that every synonym word shares the equivilance class "is a" 
	 * we can then add subclass by saying that if the synonym relationship is not symmetrical (e.g, cow is a synonym of steer, but steer is not a synonym of cow, we
	 * say that all steers are cows, but not all cows are steers...therefore, steer is a subclass of cow).  it should be noted that this type of inference is far from
	 * perfect and in fact will add many non-meaningful relationships.  however, as our coverage goal will analyze associations from existing ontologies and compare
	 * those to the corpus, it shouldnt inadvertently impact our results
	 */
	
	//this method generates a set with words from the corpus.  it then returns a set containing each unique words from the corpus
	//@param filePath:  this is the absolute path to the file you want to use as your corpus
	public HashSet<String> fetchCorpus(String filePath) throws IOException{
		HashSet<String> wordSet = new HashSet<String>();  //we use a set here because it doesnt matter how many times a word happens, only that it appears in the corpus
		//if you want to make a coprus that changes based upon word frequency or n-grams, this has to change
		
		BufferedReader in = new BufferedReader(new FileReader(new File(filePath)));
		String[] wordsFromLine;
		
		String current = in.readLine();
		while(current != null){
			wordsFromLine = current.split(" ");
			for(String word : wordsFromLine)
				wordSet.add(word.trim());
			current = in.readLine();
		}
		return wordSet;
	}
	
	
	//@param filePath: this is the path to the thesaurus you want to you
	public ThesaurusManager generateThesaurusManager(String filePath) throws IOException{
		return new ThesaurusManager(filePath);
	}
	
	
	
	public MyOwlOntologyManager generateOwlOntologyManager(){
		return new MyOwlOntologyManager();
	}
	
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
//		java.util.Date timeStamp = new java.util.Date();
//		System.out.println(timeStamp.toString());
		
		CreateOntologyFromThesaurus cot = new CreateOntologyFromThesaurus();
		String corpusPath = "/home/nicholas/research/Experiments/DataONEpython/data/noAllNumbers_5.txt";
		String thesaurusPath = "/home/nicholas/research/Experiments/DataONEjava/synonyms/GenEnglishSynCompendiumStemmed.txt";
		
		HashSet<String> wordSet = cot.fetchCorpus(corpusPath);   //get the word set from the corpus
		ThesaurusManager tm = cot.generateThesaurusManager(thesaurusPath);  //get the thesaurus
		MyOwlOntologyManager owl = cot.generateOwlOntologyManager();  //create an ontology manager
		
		OWLOntologyManager manager = owl.shouldCreateOntology("corpusOntology");//initialize empty ontology 
//		OWLOntology ontology = manager.getOntology(owl.getCurrentOntologyID());
		
		//for each word in the word set, make it a class in the ontology
		cot.addClasses(wordSet, owl, manager);
		
		//one by one add "equal to" references from thesaurus
		cot.addEquivilentClasses(wordSet, owl, manager, tm);
		
		//one by one add "subclass" references from thesaurus
		
		//save ontology
		owl.shouldSaveOntologies(manager, "/home/nicholas/research/Experiments/DataONEjava/corpus.owl");  //re-save now that we have new data
		System.out.println("i have gathered the corpus, the synonyms, and made an ontology of it.  im DONE!");
	}
	
	//given a set of words, add them all individually as classes into the ontology
	public void addClasses(HashSet<String> wordSet, MyOwlOntologyManager owl, OWLOntologyManager manager){
		for(String currentWord : wordSet){
			owl.shouldAddClass(manager, currentWord);
		}
	}
	
	//given a set of words, and a thesaurus, add each class from teh thesaurus into the ontology, and then connect all words as eqivilent
	//@param wordSet the words that already exist in the ontology
	//@param owl access to myManager class
	//@param manager access to the manager of the ontologies as decided by the API
	//@param tm access to the thesaurus manager
	public void addEquivilentClasses(HashSet<String> wordSet, MyOwlOntologyManager owl, OWLOntologyManager manager, ThesaurusManager tm) throws IOException{
		int counter = 0;
		
		for(String currentWord : wordSet){
			counter ++;
			
			if (counter % 100 == 0)
				System.out.println("we have finished " + Integer.toString(counter) + " words with their synonyms, out of " + Integer.toString(wordSet.size()));
			
			HashSet<String> synonyms = tm.getSynonyms(currentWord);
			
			if (synonyms.isEmpty()) //this word had no synonyms, move on
				continue;
			
			addClasses(synonyms, owl, manager); //add all the synonyms into the ontology		
			ArrayList<String> equivalents = new ArrayList<String>();
			equivalents.addAll(synonyms);
			equivalents.add(currentWord);
			
			for(int i =0; i < equivalents.size(); i++){//every word in this set is "equivalent" to each other word...but dont bother doing it to itself.
				for(int j = 0; j < equivalents.size(); j++){
					if (i != j){
						
						//these could be passed into the method, but for readability, i make them separate calls
						OWLClass cls1 = owl.getClassFromName(manager, equivalents.get(i)); 
						OWLClass cls2 = owl.getClassFromName(manager, equivalents.get(j));
						if ( cls1 == null || cls2 == null) //one of the classes has weird characters that cannot be represented, move on
							continue;
						
						owl.shouldAddEquivalentClassAxiom(manager, cls1, cls2); 
					}
				}
			}//end of for loop doing equivalents
		}//end of for loop dealing with words from teh word set
	}//end of method
	
	
}
