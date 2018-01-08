package aat.common;

import java.util.function.Supplier;

public class FileData {
	
	String name;
	
	String type;
	
	long fileSize;

	
	public String getName() {
		return name;
	}



	public void setName(String fileName) {
		name = fileName;
	}



	public String getType() {
		return type;
	}



	public void setType(String fileType) {
		type = fileType;
	}

	public long getFileSize() {
		return fileSize;
	}



	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	
	public static FileData create( final Supplier<FileData> supplier ) {
		
		        return supplier.get();
		
		    } 

}
