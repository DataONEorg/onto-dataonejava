package unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import owlOntologies.CreateOntologyFromThesaurus;
import owlOntologies.MyOwlOntologyManager;

public class CreateOntologyFromThesaurusTest {


	@Test
	public void testMain() {
		//this calls buildOntologyFromScratch, just test that with the right paths
	}

	
	@Test
	//you will have to adjust these paths for the machine this is run on.  
	//NOTE!!  the corpus file can ONLY contain the word "foal"  thats it
	public void testBuildOntologyFromScratch() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		CreateOntologyFromThesaurus cot = new CreateOntologyFromThesaurus();
		String ontologyLocation = "/home/nicholas/research/Experiments/DataONEjava/outputTest.owl";
		cot.buildOntologyFromScratch("/home/nicholas/research/Experiments/DataONEpython/data/test.txt", ontologyLocation);
		//first, test that it wrote SOMETHING to the ontology location
		File f = new File(ontologyLocation);
		assertTrue(f.exists());
		
		//second, load it up and check that it contains the following classes
		MyOwlOntologyManager myOwl = new MyOwlOntologyManager();
		OWLOntologyManager manager = myOwl.loadOntologyFromFile(ontologyLocation);
		OWLClass foal = myOwl.getClassFromName(manager, "foal");  //note that if any of these 4 fail, we have a problem
		OWLClass poni = myOwl.getClassFromName(manager, "poni");  
		OWLClass hobbi = myOwl.getClassFromName(manager, "hobbi");
		OWLClass colt = myOwl.getClassFromName(manager, "colt");
		
		assertNotNull(foal); //these have failed if they are null
		assertNotNull(poni);
		assertNotNull(hobbi);
		assertNotNull(colt);
		
		
		OWLOntology ontology = manager.getOntology(myOwl.getCurrentOntologyID());
		
		
		//third, check that the following subclasses are here (or not)
		Set<OWLClassExpression> superClasses = foal.getSuperClasses(ontology);
		assertTrue(superClasses.contains(hobbi));
		assertTrue(superClasses.contains(poni));
		assertFalse(superClasses.contains(colt));
		
		
		//fourth, assert that the equivalence classes are here
		Set<OWLClassExpression> equivalenceClassesForFoal = foal.getEquivalentClasses(ontology);
		Set<OWLClassExpression> equivalenceClassesForColt = colt.getEquivalentClasses(ontology);
		Set<OWLClassExpression> equivalenceClassesForPoni = poni.getEquivalentClasses(ontology);
		
		assertTrue(equivalenceClassesForFoal.contains(colt));
		assertTrue(equivalenceClassesForColt.contains(foal));
		assertTrue(equivalenceClassesForPoni.contains(foal));
		
	}
	
	@Test
	public void testAddToOntology() throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {
		CreateOntologyFromThesaurus cot = new CreateOntologyFromThesaurus();
		String ontologyLocation = "/home/nicholas/research/Experiments/DataONEjava/outputTest.owl";
		cot.buildOntologyFromScratch("/home/nicholas/research/Experiments/DataONEpython/data/test.txt", ontologyLocation);
		
		cot.addToOntology(ontologyLocation, "genoa");
		
		MyOwlOntologyManager myOwl = new MyOwlOntologyManager();
		OWLOntologyManager manager = myOwl.loadOntologyFromFile(ontologyLocation);
		
		OWLOntology ontology = manager.getOntology(myOwl.getCurrentOntologyID());
		
		//should add class genoa,   equivalence -> genova
		OWLClass genoa = myOwl.getClassFromName(manager, "genoa");
		OWLClass genova = myOwl.getClassFromName(manager, "genova");
		assertNotNull(genoa);
		assertNotNull(genova);
		
		Set<OWLClassExpression> equivalenceClassesForGenoa = genoa.getEquivalentClasses(ontology);
		
		assertTrue(equivalenceClassesForGenoa.contains(genova));
		
		
	}


}
