package net.arpa92.fileupload.web;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class FileUploadForm implements  Validator {
	
	
	@NotEmpty(message="Inserire il nome della direcotry")
	String path;
	
	public FileUploadForm(){
		
	}
	
	

	public String getPath() {
		return path;
	}



	public void setPath(String path) {
		this.path = path;
	}



	@Override
	public boolean supports(Class<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void validate(Object arg0, Errors arg1) {
		// TODO Auto-generated method stub
		
	}
	
	

}
