package org.peerbox.watchservice.filetree;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hive2hive.core.security.EncryptionUtil;
import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.PathUtils;

public class FolderAction extends Action {

//	SortedMap<String, Action> children = new TreeMap<String, Action>();
	public FolderAction(Path filePath) {
		super(filePath);
		// TODO Auto-generated constructor stub
	}
//	
//	@Override
//	protected String computeContentHash(Path filePath) {
//		String contentHashInput = "";
//		for(Action child : children.values()){
//			contentHashInput = contentHashInput.concat(child.getContentHash());
//		}
//		byte[] contentHashRaw = EncryptionUtil.generateMD5Hash(contentHashInput.getBytes());
//		String contentHash = super.createStringFromByteArray(contentHashRaw);
//		return contentHash;
//	}
//	
//	@Override
//	public void putElement(String remainingPath){
//		String nextPathLevel = PathUtils.getNextPathFragment(remainingPath);
//		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
//		
//		Action nextLevelAction;
//		
//		//if we are at the last recursion
//		Path pathToNow = getCompletePath(nextPathLevel);
//		if(newRemainingPath.equals("")){
//			
//			if(pathToNow.toFile().isDirectory()){
//				nextLevelAction = new FolderAction(pathToNow);
//			} else {
//				nextLevelAction = new FileAction(pathToNow);
//			}
//			children.put(nextPathLevel, nextLevelAction);
//		} else {
//			nextLevelAction = children.get(nextPathLevel);
//			if(nextLevelAction == null){
//				nextLevelAction = new FolderAction(pathToNow);
//				children.put(nextPathLevel, nextLevelAction);
//			}
//			nextLevelAction.putElement(newRemainingPath);
//		}
//	}
//	
//	@Override
//	public Action deleteElement(String remainingPath){
//		String nextPathLevel = PathUtils.getNextPathFragment(remainingPath);
//		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
//		
//		Action nextLevelAction;
//		
//		//if we are at the last recursion
//		if(newRemainingPath.equals("")){
//			return nextLevelAction = children.remove(nextPathLevel);
//		} else {
//			nextLevelAction = children.get(nextPathLevel);
//			if(nextLevelAction == null){
//				return null;
//			} else {
//				return nextLevelAction.deleteElement(newRemainingPath);
//			}
//		}
//	}
//	
//	@Override
//	public void getElement(Action action){
//		
//	}
//	
//	@Override
//	public void printElement(Action action){
//		
//	}
//
//	private Path getCompletePath(String lastPathFragment){
//		String completePath = getFilePath() + File.separator + lastPathFragment;
//		return Paths.get(completePath);
//	}
}
