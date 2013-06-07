package owlOntologies;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;

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
	
	
	
	public OwlOntologyManager generateOwlOntologyManager(){
		return new OwlOntologyManager();
	}
	
	
	public static void main(String[] args) throws IOException, OWLOntologyCreationException, OWLOntologyStorageException{
//		java.util.Date timeStamp = new java.util.Date();
//		System.out.println(timeStamp.toString());
		
		CreateOntologyFromThesaurus cot = new CreateOntologyFromThesaurus();
		String corpusPath = "/home/nicholas/research/Experiments/DataONEpython/data/noAllNumbers_5.txt";
		String thesaurusPath = "/home/nicholas/research/Experiments/DataONEjava/synonyms/GenEnglishSynCompendiumStemmed.txt";
		
		HashSet<String> wordSet = cot.fetchCorpus(corpusPath);   //get the word set from the corpus
		ThesaurusManager tm = cot.generateThesaurusManager(thesaurusPath);  //get the thesaurus
		OwlOntologyManager owl = cot.generateOwlOntologyManager();  //create an ontology manager
		
		
		OWLOntologyManager manager = owl.shouldCreateOntology("corpusOntology");//initialize empty ontology 
		OWLOntology ontology = manager.getOntology(owl.getCurrentOntologyID());

		
		OWLDataFactory factory = manager.getOWLDataFactory();
		OWLClass clsA = factory.getOWLClass(IRI.create(owl.getCurrentOntologyID().getOntologyIRI() + "#" + "classA"));
		OWLClass clsB = factory.getOWLClass(IRI.create(owl.getCurrentOntologyID().getOntologyIRI() + "#" + "classB"));
		
		/*
		 * ensure we can add a class here
			owl.shouldAddSubclassAxiom(manager, clsA, clsB );
		*/
		
		owl.shouldSaveOntologies(manager, "/home/nicholas/research/Experiments/DataONEjava/corpus.owl");
		
		//for each word in the word set, make it a class in the ontology
		for(String currentWord : wordSet){
			
		}
		
		//one by one add classes to ontology
		
		//one by one add "equal to" references from thesaurus
		
		//one by one add "subclass" references from thesaurus
		
		//save ontology
		
//		java.util.Date timeStamp2 = new java.util.Date();
//		System.out.println(timeStamp2.toString());
		
		
	}
	
	
	
}
