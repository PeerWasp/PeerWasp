package org.peerbox.watchservice;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.SortedMap;
import java.util.TreeMap;

import org.hive2hive.core.security.EncryptionUtil;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class FolderComposite implements FileComponent{

	private SortedMap<String, FileComponent> children = new TreeMap<String, FileComponent>();
	private Action action;
	private Path path;
	
	public FolderComposite(Path path){
		this.path = path;
		this.action = new Action(path);
		action.setContentHash(computeContentHash());
	}
	
	@Override
	public void putComponent(String remainingPath, FileComponent component) {
		
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent;
		
		//if we are at the last recursion
		Path pathToNow = getCompletePath(nextLevelPath);
		if(newRemainingPath.equals("")){
			children.put(nextLevelPath, component);
			computeContentHash();
		} else {
			nextLevelComponent = children.get(nextLevelPath);
			if(nextLevelComponent == null){
				nextLevelComponent = new FolderComposite(pathToNow);
				children.put(nextLevelPath, nextLevelComponent);

			}
			nextLevelComponent.putComponent(newRemainingPath, component);
			computeContentHash();
		}
	} 

	@Override
	public FileComponent deleteComponent(String remainingPath) {
		
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);
		
		if(newRemainingPath.equals("")){
			return children.remove(nextLevelPath);
		} else {
			if(nextLevelComponent == null){
				return null;
			} else {
				FileComponent deletedComponent = nextLevelComponent.deleteComponent(newRemainingPath);
				computeContentHash();
				return deletedComponent;	
			}
		}
	}
	
	@Override
	public FileComponent getComponent(String remainingPath){
		
		//if the path it absolute, cut off the absolute path to the root directory!
		if(remainingPath.startsWith(path.toString())){
			remainingPath = remainingPath.substring(path.toString().length() + 1);
		}
		
		String nextLevelPath = PathUtils.getNextPathFragment(remainingPath);
		String newRemainingPath = PathUtils.getRemainingPathFragment(remainingPath);
		
		FileComponent nextLevelComponent = children.get(nextLevelPath);
		
		if(newRemainingPath.equals("")){
			return children.get(nextLevelPath);
		} else {
			if(nextLevelComponent == null){
				return null;
			} else {
				return nextLevelComponent.getComponent(newRemainingPath);
			}
		}
	}

	private String computeContentHash() {
		String tmp = "";
		for(FileComponent value : children.values()){
			tmp = tmp.concat(value.getAction().getContentHash());
		}
		
		byte[] rawHash = EncryptionUtil.generateMD5Hash(tmp.getBytes());
		String contentHash = Base64.encode(rawHash);
		getAction().setContentHash(contentHash);
		
		return contentHash;
	}
	
//	private String computeContentHash(Path filePath) {
//		if(filePath != null && filePath.toFile() != null){
//			try {
//				byte[] rawHash = EncryptionUtil.generateMD5Hash(filePath.toFile());
//				if(rawHash != null){
//					return Action.createStringFromByteArray(rawHash);
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return Action.createStringFromByteArray(new byte[1]);
//	}

	@Override
	public Action getAction() {
		return action;
	}
	
	private Path getCompletePath(String lastPathFragment){
		String completePath = action.getFilePath() + File.separator + lastPathFragment;
		return Paths.get(completePath);
	}

	@Override
	public String getContentHash() {
		return action.getContentHash();
	}

}
