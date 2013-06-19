package owlOntologies;

import java.util.ArrayList;
import java.util.HashSet;
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
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

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
	public void calculateScore(String corpOnt, String testOnt) throws OWLOntologyCreationException{
		//load each ontology 
		//these three load up the ontology that represents our corpus
		MyOwlOntologyManager corpusOwl = new MyOwlOntologyManager();
		OWLOntologyManager corpusManager = corpusOwl.loadOntologyFromFile(corpOnt);
		OWLOntology corpusOntology = corpusManager.getOntology(corpusOwl.getCurrentOntologyID());
				
		//these three load up the ontology that represents our ontology under test
		MyOwlOntologyManager testOwl = new MyOwlOntologyManager();
		OWLOntologyManager testManager = testOwl.loadOntologyFromFile(testOnt);
		OWLOntology testOntology = testManager.getOntology(testOwl.getCurrentOntologyID());
		
		Set<OWLClass> testClasses = new HashSet<OWLClass>(testOwl.getClasses(testOntology));
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
		classScore = getClassScore(testClasses, new HashSet<OWLClass>(corpusOwl.getClasses(corpusOntology))) /Double.valueOf(numClasses);
		equivalenceScore = getClassEquivScore(testOntology, corpusOntology) / Double.valueOf(numEquivalences);
		subClassScore = getSubClassScore(testOntology, corpusOntology) / Double.valueOf(numSubClasses);
		
		
		
		//pretty print the scores
		System.out.println("The total number of classes in the ontology under test is " + 
					Integer.toString(numClasses) + " and it matches " + Double.toString(classScore) + " making an overall class" +
							" score of " + Double.toString(classScore/numClasses));
		
		System.out.println("The total number of equivalent classes in the ontology under test is " + 
				Integer.toString(numEquivalences) + " and it matches " + Double.toString(equivalenceScore) + " making an overall" +
						"equivalent score of " + Double.toString(equivalenceScore/numEquivalences));
		
		System.out.println("The total number of sub classes in the ontology under test is " + 
				Integer.toString(numSubClasses) + " and it matches " + Double.toString(subClassScore) + " making an overall " +
						"sub class score of " + Double.toString(subClassScore/numSubClasses));
		
		System.out.println("The total number of relations and classes in the ontology under test is " + 
				Integer.toString(numClasses + numEquivalences + numSubClasses) + " and it matches " + 
				Double.toString(classScore + equivalenceScore + subClassScore) + " making an overall BREADTH score of "
				+ Double.toString( (classScore + equivalenceScore + subClassScore) / (numClasses + numEquivalences + numSubClasses) ));
	}
	
	
	
	/*
	 * 	this method gets the score for the sub classes.  basically, for each class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of sub classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param test: the ontology under test
	 * @param corpus: the ontology representing the corpus
	 */
	public int getSubClassScore(OWLOntology test, OWLOntology corpus){
		Set<OWLSubClassOfAxiom> testSubClasses = test.getAxioms(AxiomType.SUBCLASS_OF);
		Set<OWLSubClassOfAxiom> corpusSubClasses = corpus.getAxioms(AxiomType.SUBCLASS_OF);
		
		int score = 0;
		for(OWLSubClassOfAxiom ax: testSubClasses)
			if (corpusSubClasses.contains(ax))
				score ++;
		
		return score;
	}
	
	/*
	 * this method gets the score for the classes.  basically, for each class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of equivalent classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param test: the ontology under test
	 * @param corpus: the ontology representing the corpus
	 */
	public int getClassScore(Set<OWLClass> test, Set<OWLClass> corpus){
		int score = 0;
		for(OWLClass cls: test)
			if (corpus.contains(cls))
				score ++;
		
		return score;
	}
	
	
	/*
	 * this method gets the score for the equivalent classes.  basically, for each equivalent class in the ontology under test,
	 * if it exists in the corpus, we add 1, otherwise we dont.  then we divide by the number of equivalent classes.  the idea
	 * is to get a score representing how well the ontology "represents" the corpus
	 * @param test: the ontology under test
	 * @param corpus: the ontology representing the corpus
	 */
	public int getClassEquivScore(OWLOntology test, OWLOntology corpus){
		Set<OWLEquivalentClassesAxiom> testEquivClasses = test.getAxioms(AxiomType.EQUIVALENT_CLASSES);
		Set<OWLEquivalentClassesAxiom> corpusEquivClasses = corpus.getAxioms(AxiomType.EQUIVALENT_CLASSES);
		
		int score = 0;
		for(OWLEquivalentClassesAxiom ax: testEquivClasses)
			if (corpusEquivClasses.contains(ax))
				score ++;
		
		return score;
	}
	
	

	

}
