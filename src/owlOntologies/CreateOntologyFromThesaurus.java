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

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.util.OWLOntologyMerger;


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
	
	
	//@param args:  args[0] needs to be the absolute path to the corpus, and 
	//args[1] needs to be the absolute path to where you want it stored
	//args[2] needs to be "true" or "false" on whether you want equivalent axioms or not
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
//		java.util.Date timeStamp = new java.util.Date();
//		System.out.println(timeStamp.toString());
		String corpusPath = args[0];
		String storagePath = args[1];
		boolean equivalent = Boolean.valueOf(args[2]);
		
		CreateOntologyFromThesaurus cot = new CreateOntologyFromThesaurus();
		cot.buildOntologyFromScratch(corpusPath, storagePath, equivalent);
	
//		cot.addToOntology("/home/nicholas/research/Experiments/DataONEjava/corpus.owl", "rams");
	}
	
	
	/*
	 * @param firstOntPath: the absolute path to the first ontology file
	 * @param secondOntPath: the absolute path to the second ontology file
	 * @param outputPath: the path and name of the second ontology file
	 * this takes in existing ontology files and merges their elements.  It then stores the newly generated ontology.
	 */
	public void mergeOntology(String firstOntPath, String secondOntPath, String outputPath) throws OWLOntologyStorageException, OWLOntologyCreationException{
		// Just load two arbitrary ontologies for the purposes of this example
        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        man.loadOntologyFromOntologyDocument(new File(firstOntPath));
        man.loadOntologyFromOntologyDocument(new File(secondOntPath));
        
        // Create our ontology merger
        OWLOntologyMerger merger = new OWLOntologyMerger(man);
        // We merge all of the loaded ontologies. Since an OWLOntologyManager is an OWLOntologySetProvider we just pass this in. 
        //We also need to specify the URI of the new ontology that will be created.
        IRI mergedOntologyIRI = IRI.create(outputPath);
        OWLOntology merged = merger.createMergedOntology(man, mergedOntologyIRI);
        // Print out the axioms in the merged ontology.

        // Save to RDF/XML
        man.saveOntology(merged, new RDFXMLOntologyFormat(), IRI.create("file:" + outputPath));
	}
	
	
	/*
	 * given an existing ontology, add a class to it.  this not only adds the class itself, but adds any synonyms, all equivalent classes
	 * and all subclasses.  
	 * @param ontPath: the absolute file path to the existing ontology
	 * @param className the name of the class you want to add (e.g., car, or wonderstruck)
	 */
	public void addToOntology(String ontPath, String className) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException{
		
		String thesaurusPath = "/home/nicholas/research/Experiments/DataONEjava/synonyms/GenEnglishSynCompendiumStemmed.txt";
		ThesaurusManager tm = generateThesaurusManager(thesaurusPath);  //get the thesaurus
		MyOwlOntologyManager owl = generateOwlOntologyManager();  //create an ontology manager
		OWLOntologyManager manager = owl.loadOntologyFromFile(ontPath);
		
		owl.shouldAddClass(manager, className);
		HashSet<String> classes = new HashSet<String>();  //put out word in a hashSet to use existing correct functionality  
		classes.add(className);  
		
		addEquivilentClasses(classes, owl, manager, tm);
		
		//add subclasses
		addSubClasses(classes, owl, manager, tm);

		owl.shouldSaveOntologies(manager, ontPath);  //re-save now that we have new data
		System.out.println("i took the existing ontology and added your specific class and all its implications.  im DONE!");
		
	}
	
	
	
	
	/*
	 * given a path to the corpus, build an ontology from it and store it at the requested location.  this adds each word in the corpus
	 * document, all the synonyms, all the equivalence between them, and then checks for subclasses and adds those too.  its takes quite
	 * some time to finish
	 * @param corpusPath: the absolute path to the corpus
	 * @param storagePath: the absolute path to the location where you want to save the owl ontology
	 * @param equivalent: whether or not you want to add equivalent axioms
	 */
	public void buildOntologyFromScratch(String corpusPath, String storagePath, boolean equivalent) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
		
		String thesaurusPath = "/home/nicholas/research/Experiments/DataONEjava/synonyms/GenEnglishSynCompendiumStemmed.txt";
		
		HashSet<String> wordSet = fetchCorpus(corpusPath);   //get the word set from the corpus
		ThesaurusManager tm = generateThesaurusManager(thesaurusPath);  //get the thesaurus
		MyOwlOntologyManager owl = generateOwlOntologyManager();  //create an ontology manager
		
		OWLOntologyManager manager = owl.shouldCreateOntology("corpusOntology");//initialize empty ontology 
//		OWLOntology ontology = manager.getOntology(owl.getCurrentOntologyID());
		
		//for each word in the word set, make it a class in the ontology
		addClasses(wordSet, owl, manager);
		

		if(equivalent)
			addEquivilentClasses(wordSet, owl, manager, tm);//one by one add "equal to" references from thesaurus
		else
			addSynonymClasses(wordSet, owl, manager, tm);//just add the synonyms as classes, dont worry about the equal to references
		
		//one by one add "subclass" references from thesaurus
		addSubClasses(wordSet, owl, manager, tm);
		
		//save ontology
		owl.shouldSaveOntologies(manager, storagePath);  //re-save now that we have new data
		System.out.println("i have gathered the corpus, the synonyms, and made an ontology of it.  im DONE!");
	}
	
	
	
	//given a set of words, add them all individually as classes into the ontology
	public void addClasses(HashSet<String> wordSet, MyOwlOntologyManager owl, OWLOntologyManager manager){
		for(String currentWord : wordSet){
			owl.shouldAddClass(manager, currentWord);
		}
	}
	
	

	
	
	/*
	 * given a set of words, go through and see if there is symmetry within the synonyms.  for example, assume you have two words, A, and B
	 * if A is a synonym of B, but B is not a synonym of A, then we say that A is a subclass of B
	 * note that using this will add a lot of unreasonable classes, but shouldnt hurt the overall scores and should help as the ontology we
	 * test shouldnt have these random associations
	 * 	@param wordSet the words that already exist in the ontology
	 *	@param owl access to myManager class
     *	@param manager access to the manager of the ontologies as decided by the API
	 *	@param tm access to the thesaurus manager
	 */
	public void addSubClasses(HashSet<String> wordSet, MyOwlOntologyManager owl, OWLOntologyManager manager, ThesaurusManager tm) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
		int counter = 0;
		HashSet<String> synonyms;
		
		for(String currentWord : wordSet){
			counter ++;
			if (counter % 100 == 0)
				System.out.println("we have finished " + Integer.toString(counter) + " words for subclasses, out of " + Integer.toString(wordSet.size()));
			
			synonyms = tm.getSynonyms(currentWord);
			
			for(String currentSynonym: synonyms){
				if (tm.getSynonyms(currentSynonym).contains(currentWord)) //both words are synonyms of each other, move on
					continue;
				else{ //the currentWord is a subclass of the currentSynonym
					OWLClass cls1 = owl.getClassFromName(manager, currentWord); 
					OWLClass cls2 = owl.getClassFromName(manager, currentSynonym);
					
					if(cls1 == null || cls2 == null) //if one of the super classes/sub classes had a weird name that breaks OWL, just move on
						continue;
					
					owl.shouldAddSubclassAxiom(manager, cls1, cls2);
				}
			}//end of currentSynonym for loop
		}//end of currentWord for loop
	}
	
	
	
	/*
	 * given a set of words, add all the synonyms as classes, but dont do any equivilance associations
	 * note that using this will add a lot of unreasonable classes, but shouldnt hurt the overall scores and should help as the ontology we
	 * test shouldnt have these random associations
	 * 	@param wordSet the words that already exist in the ontology
	 *	@param owl access to myManager class
     *	@param manager access to the manager of the ontologies as decided by the API
	 *	@param tm access to the thesaurus manager
	 */
	public void addSynonymClasses(HashSet<String> wordSet, MyOwlOntologyManager owl, OWLOntologyManager manager, ThesaurusManager tm) throws IOException{
		int counter = 0;
		
		for(String currentWord : wordSet){
			counter ++;
			
			if (counter % 100 == 0)
				System.out.println("we have finished " + Integer.toString(counter) + " words with their synonyms, out of " + Integer.toString(wordSet.size()));
			
			HashSet<String> synonyms = tm.getSynonyms(currentWord);
			
			if (synonyms.isEmpty()) //this word had no synonyms, move on
				continue;
			
			addClasses(synonyms, owl, manager); //add all the synonyms into the ontology
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
