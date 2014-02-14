import com.github.harunhasdal.LCAGenerator;
import static junit.framework.Assert.*;
import org.dom4j.*;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.crypto.NodeSetData;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LCAGeneratorTest {

	static Map<String,String> namespaceUris = null;

	@BeforeClass
	public static void setNamespaceUris(){
		namespaceUris = new HashMap<String, String>();
		namespaceUris.put("ns",LCAGenerator.NAMESPACE_URI);
	}

	private XPath getXPath(String path){
		XPath xPath = DocumentHelper.createXPath(path);
		xPath.setNamespaceURIs(namespaceUris);
		return xPath;
	}

	@Test
	public void throwsExceptionWhenNoBaseDirectorySpecified() throws Exception {
		try {
			new LCAGenerator(null);
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("base directory"));
		}
	}

	@Test
	public void throwsExceptionWhenBaseDirectoryInvalid() throws Exception {
		try {
			new LCAGenerator(new File("NonExistent"));
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("base directory"));
		}
	}

	@Test
	public void throwsExceptionWhenBaseDirectoryNotADirectory() throws Exception {
		try {
			new LCAGenerator(new File("pom.xml"));
			fail("Should throw IllegalArgumentException");
		} catch (IllegalArgumentException e) {
			assertTrue(e.getMessage().contains("base directory"));
		}
	}

	@Test
	public void generatesAppInfoXMLRoot() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		Element rootElement = doc.getRootElement();
		assertNotNull(rootElement);
		assertEquals(LCAGenerator.LCA_INFO, rootElement.getName());
		assertEquals(LCAGenerator.NAMESPACE_URI, rootElement.getNamespaceURI());
	}

	@Test
	public void generatesAppInfoXMLMainProperties() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		Node node = getXPath("/ns:lca_info/ns:type").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("Multiple", node.getStringValue());

		node = getXPath("/ns:lca_info/ns:description").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("sample description", node.getStringValue());

		node = getXPath("/ns:lca_info/ns:createdBy").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("test", node.getStringValue());

		node = getXPath("/ns:lca_info/ns:createdDate").selectSingleNode(doc);
		assertNotNull(node);

		Date date = LCAGenerator.TIMESTAMP_FORMAT.parse(node.getStringValue());
		assertNotNull(date);
	}

	@Test
	public void generatesAppInfoXMLMainPropertiesSingle() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"), true);
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		Node node = getXPath("/ns:lca_info/ns:type").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("Simple", node.getStringValue());
	}

	@Test
	public void generatesAppInfoXMLByDefaultForAllApplications() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"), true);
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		List nodes = getXPath("/ns:lca_info/ns:application-info").selectNodes(doc);
		assertEquals(2, nodes.size());
		Node app1 = getXPath("/ns:lca_info/ns:application-info[ns:name='TestApplication']").selectSingleNode(doc);
		assertNotNull(app1);

		Node app2 = getXPath("/ns:lca_info/ns:application-info[ns:name='TestAssets']").selectSingleNode(doc);
		assertNotNull(app1);
	}
}
