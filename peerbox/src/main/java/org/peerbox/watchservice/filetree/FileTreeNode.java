package org.peerbox.watchservice.filetree;

import java.util.SortedMap;
import java.util.TreeMap;

import org.peerbox.watchservice.Action;
import org.peerbox.watchservice.PathUtils;

public class FileTreeNode {
	Action data;
	//Vector<FileTreeNode> nodes;
	SortedMap<String, FileTreeNode> indexMap = new TreeMap<String, FileTreeNode>();
	
	public FileTreeNode(){
		this.data = null;	
	}
	
	public FileTreeNode(Action data){
		this.data = data;
	}
	
	private void setData(Action newData){
		data = newData;
	}
	
	public Action getData(){
		return data;
	}
	
	public void put(String path, Action toPut){
		
		String nextPathPart = PathUtils.getNextPathFragment(path);
		String remainingPathPart = PathUtils.getRemainingPathFragment(path);
		FileTreeNode currentNode = indexMap.get(nextPathPart);
		
		if(currentNode == null){
			currentNode = new FileTreeNode();
			indexMap.put(nextPathPart, currentNode);
		}
		
		if(remainingPathPart.equals("")){
			currentNode = indexMap.get(nextPathPart);
			currentNode.setData(toPut);
			indexMap.put(nextPathPart, currentNode);
		} else {
			currentNode.put(remainingPathPart, toPut);
		}
		
		//traverse further
	}
	
	public boolean delete(String path){
		String nextPathPart = PathUtils.getNextPathFragment(path);
		String remainingPathPart = PathUtils.getRemainingPathFragment(path);
		FileTreeNode currentNode = indexMap.get(nextPathPart);
		
		if(!remainingPathPart.equals("")){
			if(currentNode == null){
				return false;
			} else {
				return currentNode.delete(remainingPathPart);
			}
		} else {
			indexMap.remove(nextPathPart);
			return true;
		}
	}
	
	public FileTreeNode get(String path){
		String nextPathPart = PathUtils.getNextPathFragment(path);
		String remainingPathPart = PathUtils.getRemainingPathFragment(path);
		FileTreeNode currentNode = indexMap.get(nextPathPart);
		
		if(!remainingPathPart.equals("")){
			if(currentNode == null){
				return null;
			} else {
				return currentNode.get(remainingPathPart);
			}
		} else {
			if(currentNode == null){
				return null;
			} else {
				return currentNode;
			}
		}
	}
	
	public void print(){
		for(String key : indexMap.keySet()){
			String dataInfo;
			if(indexMap.get(key).getData() == null){
				dataInfo = "NULL";
			} else {
				dataInfo = indexMap.get(key).getData().getCurrentState().getClass().toString();
			}
			System.out.println(key + ": " + dataInfo);
			indexMap.get(key).print();
		}
	}
}
