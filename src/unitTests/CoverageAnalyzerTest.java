package unitTests;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import owlOntologies.CoverageAnalyzer;
import owlOntologies.MyOwlOntologyManager;

public class CoverageAnalyzerTest {
	
	//corpus ontology1
	MyOwlOntologyManager corpusOwl1;
	OWLOntologyManager corpusManager1;
	OWLOntology corpusOntology1;
	
	//corpus ontology2
	MyOwlOntologyManager corpusOwl2;
	OWLOntologyManager corpusManager2;
	OWLOntology corpusOntology2;
	
			
	//test ontology1
	MyOwlOntologyManager testOwl1;
	OWLOntologyManager testManager1;
	OWLOntology testOntology1;
	
	//test ontology2
	MyOwlOntologyManager testOwl2;
	OWLOntologyManager testManager2;
	OWLOntology testOntology2;
	
	CoverageAnalyzer ca = new CoverageAnalyzer();
	String corpus1Path;
	String corpus2Path;
	String ontology1Path;
	String ontology2Path;
	
	@Before
	public void initObjects() throws OWLOntologyCreationException{
		
		corpus1Path = "/home/nicholas/research/Experiments/DataONEjava/unitTestData/corpusForCoverageAnalyzerTest1.owl";
		corpus2Path = "/home/nicholas/research/Experiments/DataONEjava/unitTestData/corpusForCoverageAnalyzerTest2.owl";
		ontology1Path = "/home/nicholas/research/Experiments/DataONEjava/unitTestData/ontologyForCoverageAnalyzerTest1.owl";
		ontology2Path = "/home/nicholas/research/Experiments/DataONEjava/unitTestData/ontologyForCoverageAnalyzerTest2.owl";
		
		
		//corpus ontology1
		corpusOwl1 = new MyOwlOntologyManager();
		corpusManager1 = corpusOwl1.loadOntologyFromFile(corpus1Path);
		corpusOntology1 = corpusManager1.getOntology(corpusOwl1.getCurrentOntologyID());
		
		//corpus ontology2
		corpusOwl2 = new MyOwlOntologyManager();
		corpusManager2 = corpusOwl2.loadOntologyFromFile(corpus2Path);
		corpusOntology2 = corpusManager2.getOntology(corpusOwl2.getCurrentOntologyID());
		
				
		//test ontology1
		testOwl1 = new MyOwlOntologyManager();
		testManager1 = testOwl1.loadOntologyFromFile(ontology1Path);
		testOntology1 = testManager1.getOntology(testOwl1.getCurrentOntologyID());
		
		//test ontology2
		testOwl2 = new MyOwlOntologyManager();
		testManager2 = testOwl2.loadOntologyFromFile(ontology2Path);
		testOntology2 = testManager2.getOntology(testOwl2.getCurrentOntologyID());
	}
	
	
	
	@Test
	public void testCalculateScore() throws OWLOntologyCreationException {
		ca.calculateScore(corpus1Path, ontology1Path);
		assertEquals(1, ca.getClassScore(), 0.001);
		assertEquals(1, ca.getSubclassScore(), 0.001);
		assertEquals(1, ca.getEquivalenceScore(), 0.001);
		
		ca.reset();
		
		ca.calculateScore(corpus2Path, ontology2Path);
		assertEquals(5.0/27, ca.getClassScore(), 0.001);
		assertEquals(2.0/15, ca.getSubclassScore(), 0.001);
		assertEquals(10.0/241, ca.getEquivalenceScore(), 0.001);
		
		ca.reset();
		
	}

	@Test
	public void testGetSubClassScore() {
		ca.reset();
		assertEquals(2, ca.getSubClassScore(testOntology1, corpusOntology1));
		ca.reset();
		assertEquals(2, ca.getSubClassScore(testOntology2, corpusOntology2)); //the last number is the delta 
		ca.reset();
	}

	@Test
	public void testGetClassScoreSetOfOWLClassSetOfOWLClass() {
		ca.reset();
		assertEquals(5, ca.getClassScore( new HashSet(testOwl1.getClasses(testOntology1)), new HashSet(corpusOwl1.getClasses(corpusOntology1))) );
		ca.reset();
		assertEquals(5, ca.getClassScore( new HashSet(testOwl2.getClasses(testOntology2)), new HashSet(corpusOwl2.getClasses(corpusOntology2))) );	
		ca.reset();	
	}

	@Test
	public void testGetClassEquivScore() {
		ca.reset();
		assertEquals(10, ca.getClassEquivScore(testOntology1, corpusOntology1));
		ca.reset();
		assertEquals(10, ca.getClassEquivScore(testOntology2, corpusOntology2)); //the last number is the delta 
		ca.reset();
	}

}
