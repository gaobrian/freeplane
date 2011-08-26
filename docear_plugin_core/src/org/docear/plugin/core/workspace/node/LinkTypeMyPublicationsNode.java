/**
 * author: Marcel Genzmehr
 * 17.08.2011
 */
package org.docear.plugin.core.workspace.node;

import java.io.File;
import java.net.URI;
import java.net.URL;

import org.docear.plugin.core.DocearController;
import org.docear.plugin.core.event.DocearEvent;
import org.docear.plugin.core.event.DocearEventType;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.plugin.workspace.config.node.LinkNode;
import org.freeplane.plugin.workspace.controller.IWorkspaceNodeEventListener;
import org.freeplane.plugin.workspace.controller.WorkspaceNodeEvent;
import org.freeplane.plugin.workspace.io.annotation.ExportAsAttribute;

/**
 * 
 */
public class LinkTypeMyPublicationsNode extends LinkNode implements IWorkspaceNodeEventListener {

	private URI linkPath;
	
	
	/***********************************************************************************
	 * CONSTRUCTORS
	 **********************************************************************************/

	public LinkTypeMyPublicationsNode(String type) {
		super(type);
	}
	
	/***********************************************************************************
	 * METHODS
	 **********************************************************************************/

	@ExportAsAttribute("path")
	public URI getLinkPath() {
		return linkPath;
	}
	
	public void setLinkPath(URI linkPath) {
		this.linkPath = linkPath;
		if(this.linkPath != null) {
			DocearEvent event = new DocearEvent(this, DocearEventType.LIBRARY_NEW_MINDMAP_INDEXING_REQUEST, getLinkPath());
			DocearController.getController().dispatchDocearEvent(event);
		}
	}
	
	private boolean createNewMindmap(final File f) {
		if (!createFolderStructure(f)) {
			return false;
		}

		MFileManager mFileManager = MFileManager.getController(Controller.getCurrentModeController());
		mFileManager.newMap();
		MapModel map = Controller.getCurrentController().getMap();
		map.getRootNode().setText(getName());
		
		mFileManager.save(Controller.getCurrentController().getMap(), f);
		Controller.getCurrentController().close(false);

		LogUtils.info("New Mindmap Created: " + f.getAbsolutePath());
		return true;
	}

	private boolean createFolderStructure(final File f) {
		final File folder = f.getParentFile();
		if (folder.exists()) {
			return true;
		}
		return folder.mkdirs();
	}
	
	
	/***********************************************************************************
	 * REQUIRED METHODS FOR INTERFACES
	 **********************************************************************************/
	
	public void handleEvent(WorkspaceNodeEvent event) {
		System.out.println("event: "+event.getType());
		if (event.getType() == WorkspaceNodeEvent.MOUSE_LEFT_DBLCLICK) {
			System.out.println("doublecklicked MindmapNode");
			try {
				URL absoluteUrl = getLinkPath().toURL().openConnection().getURL();				
				File f = new File(absoluteUrl.getFile());
				if (!f.exists()) {
					createNewMindmap(f);
				}
				Controller.getCurrentModeController().getMapController().newMap(absoluteUrl, false);

			}
			catch (Exception e) {
				LogUtils.warn("could not open document (" + getLinkPath() + ")", e);
			}
		}
	}
}