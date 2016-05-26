package org.libertyeiffel.eclipse.test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.CoreException;
import org.junit.Assert;
import org.junit.Test;
import org.libertyeiffel.eclipse.natures.LEProjectSupport;
import org.libertyeiffel.eclipse.natures.ProjectNature;

public class LEProjectSupportTest {
	
	@Test
	public void testCreateProjectWithDifferentLocationArg() throws URISyntaxException, DocumentException, 
		CoreException {
			String workspaceFilePath = "/home/fabu/junit-workspace2";
			File worksapce = createTempWorkspace(workspaceFilePath);
			
			String projectName = "delete-me";
			String projectPath = workspaceFilePath + "/" + projectName;
			URI location = new URI("file:/" + projectPath);
			
			assertProjectDotFileAndStructureAndNatureExist(projectPath, projectName, location);
			deleteTempWorkspace(worksapce);
	}
	
	@Test
	public void testCreateProjectWithEmptyNameArg() {
		String projectName = " ";
		assertCreateProjectWithBadNameArg(projectName);
	}
	
	@Test
	public void testCreateProjectWithNullArg() {
		String projectName = null;
		assertCreateProjectWithBadNameArg(projectName);
	}
	
	@Test
	public void testCreateProjectWithGoodArgs() throws DocumentException, CoreException {
		String workspaceFilePath = "/home/fabu/junit-workspace";
		String projectName = "delete-me";
		String projectPath = workspaceFilePath + "/" + projectName;
		
		URI location = null;
		assertProjectDotFileAndStructureAndNatureExist(projectPath, projectName, location);
	}
	
	private void assertProjectDotFileAndStructureAndNatureExist(String projectPath, String name, URI location) throws
		DocumentException, CoreException {
			IProject project = LEProjectSupport.createProject(name, location);
			
			String projectFilePath = projectPath + "/" + ".project";
			String[] emptyNodes = { "/projectDescription/comment", "/projectDescription/projects", "/projectDescription/buildSpec" };
	        String[] nonEmptyNodes = { "/projectDescription/name", "/projectDescription/natures/nature" };
	        
	        Assert.assertNotNull(project);
	        assertFileExists(projectPath);
	        assertAllElementsEmptyExcept(projectFilePath, emptyNodes, nonEmptyNodes);
	        assertNatureIn(project);
	        assertFolderStructureIn(projectPath);
	        
	        project.delete(true, null);
	}
	
	private void assertFolderStructureIn(String projectPath) {
		String[] paths = { "bin", "src" };
		for (String path : paths) {
			File file = new File(projectPath + "/" + path);
			if (!file.exists()) {
				Assert.fail("Folder structure " + path + "does not exist.");
			}
		}
	}
	
	private void assertNatureIn(IProject project) throws CoreException {
		IProjectDescription descriptions = project.getDescription();
		String[] natureIds = descriptions.getNatureIds();
		if (natureIds.length != 1) {
			Assert.fail("No natures found in project.");
		}
		
		if (!natureIds[0].equals(ProjectNature.NATURE_ID)) {
			Assert.fail("Liberty Eiffel Project natures not found in project");
		}
	}
	
	private void assertAllElementsEmptyExcept(String projectFilePath, String[] emptyNodes, String[] nonEmptyNodes) throws
		DocumentException {
			SAXReader reader = new SAXReader();
			Document document = reader.read(projectFilePath);
			int strLength;
			for (String emptyNode : emptyNodes) {
				strLength = document.selectSingleNode(emptyNode).getText().trim().length();
				if (strLength != 0) {
					Assert.fail("Node " + emptyNode + " was non-empty!");
				}
			}
			
			for (String nonEmptyNode : nonEmptyNodes) {
				strLength = document.selectSingleNode(nonEmptyNode).getText().trim().length();
				if (strLength == 0) {
					Assert.fail("Node " + nonEmptyNode + " was empty");
				}
			}
	}
	
	private void assertFileExists(String projectFilePath) {
		File file =  new File(projectFilePath);
		
		if (!file.exists()) {
			Assert.fail("File " + projectFilePath + "does not exits.");
		}
	}
	
	private void assertCreateProjectWithBadNameArg(String name) {
		URI location = null;
		try {
			LEProjectSupport.createProject(name, location);
			Assert.fail("The call to LEProjectSupport.createProject() did not fail");
		} catch (AssertionFailedException e) {
			// TODO: handle exception
		}
	}
	
	private void deleteTempWorkspace(File workspace) {
		boolean deleted = workspace.delete();
		if (!deleted) {
			Assert.fail("Unable to delete the new workspace dir at " + workspace);
		}
	}
	
	private File createTempWorkspace(String pathToWorkspace) {
		File workspace = new File(pathToWorkspace);
		if (!workspace.exists()) {
			boolean dirCreated = workspace.mkdir();
			if (!dirCreated) {
				Assert.fail("Unable to create the new worksapce dir at " + workspace);
			}
		}
		
		return workspace;
		
	}
}
