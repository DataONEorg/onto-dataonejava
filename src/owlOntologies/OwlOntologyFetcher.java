package owlOntologies;

import java.awt.List;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SetOntologyID;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;




public class OwlOntologyFetcher {
	
	
	public OwlOntologyFetcher(){
		
	}
	
	
	public static void main(String[] args){
		OwlOntologyFetcher owl = new OwlOntologyFetcher();
		try {
			OWLOntologyManager manager = owl.loadOntologyFromFile("/home/nicholas/Desktop/temp/matr.owl");
			OWLOntology ontology = manager.getOntologies().iterator().next();
			ArrayList<OWLClass> classes = owl.getNamesOfClasses(ontology);
			
			System.out.println( "classes to test are : " +  classes.get(0).toString() + " " +  classes.get(1).toString());
			owl.shouldAddSubclassAxiom(manager, classes.get(0), classes.get(1));
			System.out.println(owl.isSuperClass(manager, classes.get(0), classes.get(1)));
			System.out.println(owl.isSuperClass(manager, classes.get(1), classes.get(0)));
			
			
			
			
		} catch (Exception e) {
			System.out.println("hrmmm");
		}
	}
	
	
	//given an ontology it will return the classes as an arrayList of classes.  note that the commented out line will return the name of the classes without the prefix
	public ArrayList<OWLClass> getNamesOfClasses(OWLOntology ontology){
		
		ArrayList<OWLClass> classes = new ArrayList<OWLClass>();
		for (OWLClass cls : ontology.getClassesInSignature())
			//classes.add( cls.getIRI().getFragment() );
			classes.add(cls);
		return classes;
	}
	
	//loads an ontology and returns the manager for it
	public OWLOntologyManager loadOntologyFromURL(String URL) throws OWLOntologyCreationException{
		// Get hold of an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// Let's load an ontology from the web
		IRI iri = IRI.create(URL);
		OWLOntology URLOntology = manager.loadOntologyFromOntologyDocument(iri);
		System.out.println("Loaded ontology: " + URLOntology);
		return manager;
		
		
	}

	//loads an ontology and returns the manager for it
	public OWLOntologyManager loadOntologyFromFile(String filePath) throws OWLOntologyCreationException{
		// Get hold of an ontology manager
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// We can also load ontologies from files. Download the pizza ontology
		// from http://www.co-ode.org/ontologies/pizza/pizza.owl and put it
		// somewhere on your hard drive Create a file object that points to the
		// local copy
		File file = new File(filePath);
		// Now load the local copy
		OWLOntology fileOntology = manager.loadOntologyFromOntologyDocument(file);
		System.out.println("Loaded ontology: " + fileOntology);
		return manager;
		
	}
	
	
	//creates a blank ontology with the given name and returns its manager
	public OWLOntologyManager shouldCreateOntology(String ontologyName) throws OWLOntologyCreationException {
		// We first need to create an OWLOntologyManager, which will provide a
		// point for creating, loading and saving ontologies. We can create a
		// default ontology manager with the OWLManager class. This provides a
		// common setup of an ontology manager. It registers parsers etc. for
		// loading ontologies in a variety of syntaxes
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		// In OWL 2, an ontology may be named with an IRI (Internationalised
		// Resource Identifier) We can create an instance of the IRI class as
		// follows:
		IRI ontologyIRI = IRI.create(ontologyName);
		// Here we have decided to call our ontology
		// "ontologyName" If we publish our
		// ontology then we should make the location coincide with the ontology
		// IRI Now we have an IRI we can create an ontology using the manager
		OWLOntology ontology = manager.createOntology(ontologyIRI);
		System.out.println("Created ontology: " + ontology);
		// In OWL 2 if an ontology has an ontology IRI it may also have a
		// version IRI The OWL API encapsulates ontology IRI and possible
		// version IRI information in an OWLOntologyID Each ontology knows about
		// its ID
		OWLOntologyID ontologyID = ontology.getOntologyID();
		// In this case our ontology has an IRI but does not have a version IRI
		System.out.println("Ontology IRI: " + ontologyID.getOntologyIRI());
		// Our version IRI will be null to indicate that we don't have a version
		// IRI
		System.out.println("Ontology Version IRI: " + ontologyID.getVersionIRI());
		// An ontology may not have a version IRI - in this case, we count the
		// ontology as an anonymous ontology. Our ontology does have an IRI so
		// it is not anonymous:
		System.out.println("Anonymous Ontology: " + ontologyID.isAnonymous());
		// Once an ontology has been created its ontology ID (Ontology IRI and
		// version IRI can be changed) to do this we must apply a SetOntologyID
		// change through the ontology manager. Lets specify a version IRI for
		// our ontology. In our case we will just "extend" our ontology IRI with
		// some version information. We could of course specify any IRI for our
		// version IRI.
		IRI versionIRI = IRI.create(ontologyIRI + "/version1");
		// Note that we MUST specify an ontology IRI if we want to specify a
		// version IRI
		OWLOntologyID newOntologyID = new OWLOntologyID(ontologyIRI, versionIRI);
		// Create the change that will set our version IRI
		SetOntologyID setOntologyID = new SetOntologyID(ontology, newOntologyID);
		// Apply the change
		manager.applyChange(setOntologyID);
		System.out.println("Ontology: " + ontology);
		
		return manager;
		
	}
	
	
	
	//this method takes two classes and an ontology manager, and creates an axiom where a is a subclass of b
	//@param manager: the manager can only have ONE ontology in it. 
	public void shouldAddSubclassAxiom(OWLOntologyManager manager, OWLClass classA, OWLClass classB) throws OWLOntologyCreationException,OWLOntologyStorageException {
		OWLOntology ontology = manager.getOntologies().iterator().next(); //we assert here that the manager only has ONE ontology within it
			
		// Now we want to specify that A is a subclass of B. To do this, we add
		// a subclass axiom. A subclass axiom is simply an object that specifies
		// that one class is a subclass of another class. We need a data factory
		// to create various object from. Each manager has a reference to a data
		// factory that we can use.
		OWLDataFactory factory = manager.getOWLDataFactory();
	
		// Now create the axiom
		OWLAxiom axiom = factory.getOWLSubClassOfAxiom(classA, classB);
		
		// We now add the axiom to the ontology, so that the ontology states
		// that A is a subclass of B. To do this we create an AddAxiom change
		// object. At this stage neither classes A or B, or the axiom are
		// contained in the ontology. We have to add the axiom to the ontology.
		AddAxiom addAxiom = new AddAxiom(ontology, axiom);
		// We now use the manager to apply the change
		manager.applyChange(addAxiom);
	
	}
	
	
	public void shouldCreatePropertyAssertions(OWLOntologyManager manager, OWLObjectProperty prop, OWLNamedIndividual name0, 
			OWLNamedIndividual name1) throws OWLOntologyStorageException, OWLOntologyCreationException {
		
		OWLOntology ontology = manager.getOntologies().iterator().next(); //we assert here that the manager only has ONE ontology within it
			
		// Get hold of a data factory from the manager and set up a prefix
		// manager to make things easier
		OWLDataFactory factory = manager.getOWLDataFactory();
//		PrefixManager pm = new DefaultPrefixManager(ontology.getOntologyID().getOntologyIRI().toString());

		// To specify that name1 is related to name2 via the prop property
		// we create an object property assertion and add it to the ontology
		OWLObjectPropertyAssertionAxiom propertyAssertion = factory.getOWLObjectPropertyAssertionAxiom(prop, name0, name1);
		manager.addAxiom(ontology, propertyAssertion);
		
	}
	
	
	//given a specific class and a specific individual, add an axiom so that individual is a member of that class
	//e.g., mary is the individual and person is the class.  add an axiom so mary is a person
	public void shouldAddClassAssertion(OWLOntologyManager manager, OWLClass cls, OWLNamedIndividual name) 
			throws OWLOntologyCreationException,OWLOntologyStorageException {
		
		
		OWLOntology ontology = manager.getOntologies().iterator().next(); //we assert here that the manager only has ONE ontology within it
		String base = ontology.getOntologyID().getOntologyIRI().getNamespace(); //this returns the prefix for the given class
		OWLDataFactory dataFactory = manager.getOWLDataFactory();
		// Now create a ClassAssertion to specify that name is an instance of cls
		OWLClassAssertionAxiom classAssertion = dataFactory.getOWLClassAssertionAxiom(cls, name);
		// We need to add the class assertion to the ontology that we want
		// specify that :Mary is a :Person
		ontology = manager.createOntology(IRI.create(base));
		// Add the class assertion
		manager.addAxiom(ontology, classAssertion);
	
	}
	
	
	
	
	//given two classes within an ongology, return true if classA is a subclass of class B.  in other words, b is an ASSERTED superclass of A
	public boolean isSuperClass(OWLOntologyManager manager, OWLClass classA, OWLClass classB){
		OWLOntology ontology = manager.getOntologies().iterator().next(); //we assert here that the manager only has ONE ontology within it
		
		Set<OWLClassExpression> superClasses = classA.getSuperClasses(ontology);
		return superClasses.contains(classB);
	}
	
	
	

}
