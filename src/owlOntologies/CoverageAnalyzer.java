package owlOntologies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

public class CoverageAnalyzer {

	
	private double classScore = Double.MAX_VALUE;
	private double equivalenceScore = Double.MAX_VALUE;
	private double subClassScore = Double.MAX_VALUE;
	private double breadth = Double.MAX_VALUE;
	private double depth = Double.MAX_VALUE;
	
	/**
	 * @param args
	 * @throws OWLOntologyCreationException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException, IOException {
				
		CoverageAnalyzer cov = new CoverageAnalyzer();
		cov.calculateScore(args[0], args[1]);

	}
	
	public void reset(){
		classScore = Double.MAX_VALUE;
		equivalenceScore = Double.MAX_VALUE;
		subClassScore = Double.MAX_VALUE;
		breadth = Double.MAX_VALUE;
		depth = Double.MAX_VALUE;
	}
	
	public double getClassScore(){
		return classScore;
	}
	
	public double getEquivalenceScore(){
		return equivalenceScore;
	}
	
	public double getSubclassScore(){
		return subClassScore;
	}
	
	public double getBreadth(){
		return breadth;
	}
	
	public double getDepth(){
		return depth;
	}
	
	/*
	 * @param corpOnt this is the absolute path for the ontology we created for the corpus
	 * @param testOnt this is the absolute path for the ontology we are testing
	 */
	public void calculateScore(String corpOnt, String testOnt) throws OWLOntologyCreationException, IOException{
		//load each ontology 
		//these three load up the ontology that represents our corpus
		MyOwlOntologyManager corpusOwl = new MyOwlOntologyManager();
		OWLOntologyManager corpusManager = corpusOwl.loadOntologyFromFile(corpOnt);
		OWLOntology corpusOntology = corpusManager.getOntology(corpusOwl.getCurrentOntologyID());
		
		//these three load up the ontology that represents our ontology under test
		MyOwlOntologyManager testOwl = new MyOwlOntologyManager();
		OWLOntologyManager testManager = testOwl.loadOntologyFromFile(testOnt);
		OWLOntology testOntology = testManager.getOntology(testOwl.getCurrentOntologyID());
		
		Set<String> testClasses = new HashSet<String>(testOwl.getNamesOfClasses(testOntology));
		//determine numOfClass in testOnt		
		int numClasses = testClasses.size();

		//determine numOfEquivilances in testOnt
		int numEquivalences = testOntology.getAxiomCount(AxiomType.EQUIVALENT_CLASSES);;
		
		//determine numOfSubclasses in testOnt
		int numSubClasses = testOntology.getAxiomCount(AxiomType.SUBCLASS_OF);
		
		//for each ontology in testOnt
		//check classes
		//check equivilances
		//check subclasses
		classScore = getClassScore(testClasses, new HashSet<String>(corpusOwl.getNamesOfClasses(corpusOntology))) /Double.valueOf(numClasses);
		equivalenceScore = getClassEquivScore(testOwl, testManager, testOntology, corpusOwl, corpusManager, corpusOntology) / Double.valueOf(numEquivalences);
		subClassScore = getSubClassScore(testOwl, testManager, testOntology, corpusOwl, corpusManager, corpusOntology) / Double.valueOf(numSubClasses);
		
		
		
		//pretty print the scores
		System.out.println("The total number of classes in the ontology under test is " + 
					Integer.toString(numClasses) + " and it matches " + Double.toString(classScore * Double.valueOf(numClasses)) + " making an overall class" +
							" score of " + Double.toString(classScore));
		
		System.out.println("The total number of equivalent classes in the ontology under test is " + 
				Integer.toString(numEquivalences) + " and it matches " + Double.toString(equivalenceScore * Double.valueOf(numEquivalences)) + " making an overall" +
						"equivalent score of " + Double.toString(equivalenceScore));
		
		System.out.println("The total number of sub classes in the ontology under test is " + 
				Integer.toString(numSubClasses) + " and it matches " + Double.toString(subClassScore * Double.valueOf(numSubClasses)) + " making an overall " +
						"sub class score of " + Double.toString(subClassScore));
		
		System.out.println("The total number of relations and classes in the ontology under test is " + 
				Integer.toString(numClasses + numEquivalences + numSubClasses) + " making an overall BREADTH score of "
				+ Double.toString( (classScore + equivalenceScore + subClassScore) / (3) ));
	}
	
	
	
	/*
	 * 	this method gets the score for the sub classes.  basically, for each class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of sub classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param testOwl: the MyOwlOntologyManager access for the ontology under test
	 * @param testManager: the OWL ontology manager for the ontology under test
	 * @param test: the ontology under test
	 * @param corpusOwl: the MyOwlOntologyManager access for the corpus ontology
	 * @param corpusManager: the OWL ontology manager for the corpus ontology
	 * @param corpus: the ontology representing the corpus
	 */
	public int getSubClassScore(MyOwlOntologyManager testOwl, OWLOntologyManager testManager, OWLOntology test, 
			MyOwlOntologyManager corpusOwl, OWLOntologyManager corpusManager, OWLOntology corpus) throws IOException{
		int score = 0;
		
		String normalClsName = "";
		String normalEquivName = "";
		
		HashSet<OWLClass> testClasses = new HashSet<OWLClass>();		
		testClasses.addAll(test.getClassesInSignature() );

		for (OWLClass testCls: test.getClassesInSignature()){
			normalClsName = testCls.getIRI().getFragment();
			
			OWLClass corpusCls = corpusOwl.getClassFromName(corpus, normalClsName);
			if(corpusCls == null)//if the class exists in the corpus as well...then check the equivalences 
				continue;
			
			
			Set<OWLClassExpression> subClasses = testCls.getSubClasses(test);
			Iterator<OWLClassExpression> iter = subClasses.iterator();
			
			OWLClassExpression current;
			String currentClassName;
			OWLClass currentClass;
			while(iter.hasNext()){
				current = iter.next();
				currentClassName = current.toString().split("#")[1]; //because its a "class expression" we cant call getIRI().getFragment()/...this code is the functional equivalent
				currentClassName = currentClassName.replace(">", ""); //after teh split its left with a ">" at the end...
				currentClass = corpusOwl.getClassFromName(corpus, currentClassName);
				if (currentClass == null)
					continue;
				
				if ( corpusCls.getSubClasses(corpus).contains(currentClass))
					score++;

			}	//end of while (current set of potential subClasses
		}//end of for (list of classes in ontology under test)
	
		return score; 
	}
	
	/*
	 * this method gets the score for the classes.  basically, for each class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of equivalent classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param test: the ontology under test
	 * @param corpus: the ontology representing the corpus
	 */
	public int getClassScore(Set<String> test, Set<String> corpus){
		int score = 0;
		for(String cls: test)
			if (corpus.contains(cls))
				score ++;
//			else
//				System.out.println(cls);
		
		return score;
	}
	
	
	/*
	 * this method gets the score for the equivalent classes.  basically, for each equivalent class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of equivalent classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param testOwl: the MyOwlOntologyManager access for the ontology under test
	 * @param testManager: the OWL ontology manager for the ontology under test
	 * @param test: the ontology under test
	 * @param corpusOwl: the MyOwlOntologyManager access for the corpus ontology
	 * @param corpusManager: the OWL ontology manager for the corpus ontology
	 * @param corpus: the ontology representing the corpus
	 */
	public int getClassEquivScore(MyOwlOntologyManager testOwl, OWLOntologyManager testManager, OWLOntology test, 
			MyOwlOntologyManager corpusOwl, OWLOntologyManager corpusManager, OWLOntology corpus) throws IOException{	
		int score = 0;
		
		String normalClsName = "";
		String normalEquivName = "";
		
		HashSet<OWLClass> testClasses = new HashSet<OWLClass>();		
		testClasses.addAll(test.getClassesInSignature() );

		for (OWLClass testCls: test.getClassesInSignature()){
			normalClsName = testCls.getIRI().getFragment();
			
			OWLClass corpusCls = corpusOwl.getClassFromName(corpus, normalClsName);
			if(corpusCls == null)//if the class exists in the corpus as well...then check the equivalences 
				continue;
			
			
			Set<OWLClassExpression> equivalentClasses = testCls.getEquivalentClasses(test);
			Iterator<OWLClassExpression> iter = equivalentClasses.iterator();
			
			OWLClassExpression current;
			String currentClassName;
			OWLClass currentClass;
			while(iter.hasNext()){
				current = iter.next();
				currentClassName = current.toString().split("#")[1]; //because its a "class expression" we cant call getIRI().getFragment()/...this code is the functional equivalent
				currentClassName = currentClassName.replace(">", ""); //after teh split its left with a ">" at the end...
				currentClass = corpusOwl.getClassFromName(corpus, currentClassName);
				if (currentClass == null)
					continue;
				
				if ( corpusCls.getEquivalentClasses(corpus).contains(currentClass))
					score++;

			}	//end of while (current set of potential equivalences
		}//end of for (list of classes in ontology under test)
	
		return score/2; //its divided by two because for each equivalence, it adds it twice (because it says, if a-> b  and b-> a then there are two equivalences
	}
	
	

	

}
