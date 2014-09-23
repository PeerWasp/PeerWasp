package org.peerbox.watchservice.filetree;

import java.io.IOException;
import java.nio.file.Path;

import org.hive2hive.core.security.EncryptionUtil;
import org.peerbox.watchservice.Action;

public class FileAction extends Action {
//
	public FileAction(Path filePath) {
		super(filePath);
		// TODO Auto-generated constructor stub
	}
//
//	@Override
//	protected String computeContentHash(Path filePath) {
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
}
