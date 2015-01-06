//package org.peerbox.selectivesync;
//
//import java.nio.file.Path;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
//public final class Synchronizer implements ISynchronize{
//
//	private final Map<Path, Boolean> selectiveSync = new ConcurrentHashMap<Path, Boolean>();
//	
//	@Override
//	public int synchronize(Path fileToSync) {
//		int synchedFiles = 0;
//		for(Map.Entry<Path, Boolean > entry : selectiveSync.entrySet()){
//			if(entry.getKey().startsWith(fileToSync) && entry.getValue().equals(Boolean.FALSE)){
//				entry.setValue(new Boolean(true));
//				synchedFiles++;
//			}
//		}
//		return synchedFiles;
//	}
//
//	@Override
//	public int desynchronize(Path fileToDesync) {
//		int desynchedFiles = 0;
//		for(Map.Entry<Path, Boolean > entry : selectiveSync.entrySet()){
//			if(entry.getKey().startsWith(fileToDesync) && entry.getValue().equals(Boolean.TRUE)){
//				entry.setValue(new Boolean(false));
//				desynchedFiles++;
//			}
//		}
//		return desynchedFiles;
//	}
//
//	@Override
//	public Map<Path, Boolean> getSynchronization() {
//		return selectiveSync;
//	}
//
//}
