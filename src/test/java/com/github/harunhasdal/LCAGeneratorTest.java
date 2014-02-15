package com.github.harunhasdal;

import static junit.framework.Assert.*;
import org.dom4j.*;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
		Node node = getXPath("/ns:lca_info/type").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("Multiple", node.getStringValue());

		node = getXPath("/ns:lca_info/description").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("sample description", node.getStringValue());

		node = getXPath("/ns:lca_info/createdBy").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("test", node.getStringValue());

		node = getXPath("/ns:lca_info/createdDate").selectSingleNode(doc);
		assertNotNull(node);

		Date date = LCAGenerator.TIMESTAMP_FORMAT.parse(node.getStringValue());
		assertNotNull(date);
	}

	@Test
	public void generatesAppInfoXMLMainPropertiesSingle() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"), true);
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		Node node = getXPath("/ns:lca_info/type").selectSingleNode(doc);
		assertNotNull(node);
		assertEquals("Simple", node.getStringValue());
	}

	@Test
	public void generatesAppInfoXMLByDefaultForAllApplicationVersions() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		Document doc = generator.generateArchiveInfo("sample description","test");

		assertNotNull(doc);
		List nodes = getXPath("/ns:lca_info/ns:application-info").selectNodes(doc);
		assertEquals(3, nodes.size());
		Node app1 = getXPath("/ns:lca_info/ns:application-info[name='TestApplication']").selectSingleNode(doc);
		assertNotNull(app1);
		assertEquals("1", getXPath("major-version").selectSingleNode(app1).getText());

		Node app2 = getXPath("/ns:lca_info/ns:application-info[name='TestAssets' and minor-version='0']").selectSingleNode(doc);
		assertNotNull(app2);

		Node app3 = getXPath("/ns:lca_info/ns:application-info[name='TestAssets' and minor-version='1']").selectSingleNode(doc);
		assertNotNull(app3);
	}

	@Test
	public void generatesTopLevelObjectsForApplications() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		Document doc = generator.generateArchiveInfo("sample description","test");
		assertNotNull(doc);
		List topLevelObjects = getXPath("/ns:lca_info/ns:application-info[name='TestApplication']/top-level-object").selectNodes(doc);
		assertEquals(4, topLevelObjects.size());
	}

	@Test
	public void applicationFilterAcceptsOnlyApplicationDirectories() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		FilenameFilter filter = generator.getApplicationFilter();

		assertTrue(filter.accept(new File("./test/TestAssets"), "TestAssets"));
		assertFalse(filter.accept(new File("./test/.project"), ".project"));
	}

	@Test
	public void applicationVersionFilterAcceptsOnlyVersionDirectories() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		FilenameFilter filter = generator.getApplicationFilter();

		assertTrue(filter.accept(new File("./test/TestAssets/1.0"), "1.0"));
		assertFalse(filter.accept(new File("./test/TestAssets/.application"), ".application"));
	}

	@Test
	public void generatesLCAFile() throws Exception {
		LCAGenerator generator = new LCAGenerator(new File("./test"));
		byte[] lca = generator.generateLCA("sample description","test");
		assertNotNull(lca);
		ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(lca));
		List<String> expectedFileList = new ArrayList();
		expectedFileList.add("app.info");
		expectedFileList.add("TestApplication/1.0/Events/SampleNotification.event");
		expectedFileList.add("TestApplication/1.0/Processes/SampleNotificationReceiver.process");
		expectedFileList.add("TestApplication/1.0/Processes/SampleNotificationReceiver.process_dependency");
		expectedFileList.add("TestApplication/1.0/Processes/SampleProcess.process");
		expectedFileList.add("TestApplication/1.0/Processes/SampleProcess.process_dependency");
		expectedFileList.add("TestApplication/1.0/Processes/SampleSub.process");
		expectedFileList.add("TestApplication/1.0/Processes/SampleSub.process_dependency");
		expectedFileList.add("TestAssets/1.0/forms/MemberForm.xdp");
		expectedFileList.add("TestAssets/1.0/forms/MemberForm.xdp_dci");
		expectedFileList.add("TestAssets/1.0/schemas/MemberForm.xsd");
		expectedFileList.add("TestAssets/1.0/schemas/SampleEventMessage.xsd");
		expectedFileList.add("TestAssets/1.1/forms/MemberForm.xdp");
		expectedFileList.add("TestAssets/1.1/forms/MemberForm.xdp_dci");
		expectedFileList.add("TestAssets/1.1/schemas/MemberForm.xsd");
		expectedFileList.add("TestAssets/1.1/schemas/SampleEventMessage.xsd");
		ZipEntry zipFile;
		int actualFileCount = 0;
		while ((zipFile = zis.getNextEntry()) != null) {
			assertTrue("LCA should contain " + zipFile.getName(), expectedFileList.contains(zipFile.getName()));
			actualFileCount++;
		}
		assertEquals(expectedFileList.size(), actualFileCount);
	}
}
