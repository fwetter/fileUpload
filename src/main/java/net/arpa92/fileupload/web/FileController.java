package net.arpa92.fileupload.web;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import net.arpa92.fileupload.FileMeta;

@Controller
//@RequestMapping("/controller")
public class FileController {


	@Autowired
	FileController(net.arpa92.fileupload.AmazonS3Dao s3Dao, String localUploadDir, String remoteUploadDir){
		this.s3Dao = s3Dao;
		this.localUploadDir = localUploadDir;

		if(this.localUploadDir!=null && !this.localUploadDir.endsWith(fileSeparator)){
			this.localUploadDir += fileSeparator;
		}

		this.remoteUploadDir = remoteUploadDir;

		if(this.remoteUploadDir!=null && !this.remoteUploadDir.endsWith(fileSeparator)){
			this.remoteUploadDir += fileSeparator;
		}

	}

	private String checkPath(String path){
		if(path==null) path = fileSeparator;
		if(!path.endsWith(fileSeparator)) path += fileSeparator;
		return path;
	}


	private net.arpa92.fileupload.AmazonS3Dao s3Dao;
	private String localUploadDir;
	private String remoteUploadDir;
	private String fileSeparator = System.getProperty("file.separator");

	LinkedList<FileMeta> files = new LinkedList<FileMeta>();
	FileMeta fileMeta = null;
	/***************************************************
	 * URL: /rest/controller/upload  
	 * upload(): receives files
	 * @param request : MultipartHttpServletRequest auto passed
	 * @param response : HttpServletResponse auto passed
	 * @return LinkedList<FileMeta> as json format
	 ****************************************************/
	@RequestMapping(value="/admin/upload", method = RequestMethod.POST)
	public @ResponseBody LinkedList<FileMeta> upload(MultipartHttpServletRequest request, HttpServletResponse response) {


		//String dirName = "/Users/fede/Documents/workspace-sts-3.7.3.RELEASE/allotments/src/main/webapp/resources/temp/upload/";
		//dirName = "/Volumes/SD01/tmp/";
		/*
		String path = request.getParameter("path");
		if(path==null) path = fileSeparator;
		if(!path.endsWith(fileSeparator)) path += fileSeparator;
		 */
		String path = checkPath(request.getParameter("path"));
		
		String dirName = this.localUploadDir + path;

		File uploadDir = new File(dirName);
		if(!uploadDir.exists()){
			uploadDir.mkdirs();
		}


		System.out.println("uploadDir:" + uploadDir.getAbsolutePath());



		//1. build an iterator
		Iterator<String> itr =  request.getFileNames();
		MultipartFile mpf = null;

		//2. get each file
		while(itr.hasNext()){

			//2.1 get next MultipartFile
			mpf = request.getFile(itr.next()); 
			System.out.println(mpf.getOriginalFilename() +" uploaded! "+files.size());

			//2.2 if files > 10 remove the first from the list
			if(files.size() >= 10)
				files.pop();

			//2.3 create new fileMeta
			fileMeta = new FileMeta();
			fileMeta.setFileName(mpf.getOriginalFilename());
			fileMeta.setFileSize(mpf.getSize()/1024+" Kb");
			fileMeta.setFileType(mpf.getContentType());

			try {
				fileMeta.setBytes(mpf.getBytes());

				// copy file to local disk (make sure the path "e.g. D:/temp/files" exists)  

				FileCopyUtils.copy(mpf.getBytes(), new FileOutputStream(dirName + mpf.getOriginalFilename()));



			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//2.4 add to files
			files.add(fileMeta);
		}

		// upload to s3
		File filesio = new File (dirName);
		System.out.println("inizio copia su S3");

		s3Dao.saveAsync(filesio.listFiles(), this.remoteUploadDir +  path);


		System.out.println("file copiati su S3 uploadDir:" + uploadDir.getAbsolutePath());


		System.out.println("elimino file da locale");

		files = s3Dao.getList(this.remoteUploadDir +  path);

		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonInString = mapper.writeValueAsString(files);
			System.out.println(jsonInString);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}



		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
		return files;
	}
	/***************************************************
	 * URL: /rest/controller/get/{value}
	 * get(): get file as an attachment
	 * @param response : passed by the server
	 * @param value : value from the URL
	 * @return void
	 ****************************************************/
	@RequestMapping(value = "/get/{value}", method = RequestMethod.GET)
	public void get(HttpServletResponse response,@PathVariable String value){
		FileMeta getFile = files.get(Integer.parseInt(value));
		try {      
			response.setContentType(getFile.getFileType());
			response.setHeader("Content-disposition", "attachment; filename=\""+getFile.getFileName()+"\"");
			FileCopyUtils.copy(getFile.getBytes(), response.getOutputStream());
		}catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// ================= INDEX
	@RequestMapping(value="/admin/upload.html", method=RequestMethod.GET)
	public String index(@RequestParam("path") String path,  HttpServletRequest request, Model req_model ) {

		System.out.println("ApplicationRealPath: "+ getApplicationRealPath(request) + "\t " + localUploadDir);


		//String callb = request.getParameter("callb");
		//System.out.println("index callb:" + callb);


		FileUploadForm form = new FileUploadForm();
		form.setPath(path);

		req_model.addAttribute(form);

		return "upload"; 
	}

	
	/*
	 *  @RequestMapping(value = "/mileage/locations", method = RequestMethod.GET)
    public @ResponseBody List<ZipSystem> getLocations(@RequestParam(value = "q", required = true) String cityName, @RequestParam(value="limit", required = false) String limit) {
	 */
	
	// ================= LIST DIR
	@RequestMapping(value="/admin/ls", method=RequestMethod.GET)
	public @ResponseBody LinkedList<FileMeta> getDir(@RequestParam("path") String path ) {

		//System.out.println("ApplicationRealPath: "+ getApplicationRealPath(request) + "\t " + localUploadDir);

		
		
		path = checkPath(path);

		//String callb = request.getParameter("callb");
		System.out.println("ls path:" + path);
		
		files = s3Dao.getList(this.remoteUploadDir +  path);

		ObjectMapper mapper = new ObjectMapper();
		try {
			String jsonInString = mapper.writeValueAsString(files);
			System.out.println(jsonInString);
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//response.setContentType("application/javascript");
		
		
		// result will be like this
		// [{"fileName":"app_engine-85x77.png","fileSize":"8 Kb","fileType":"image/png"},...]
		return files;
		
		//return (callBack+"("+files+")");


		//return null; 
	}

	private String getApplicationRealPath(HttpServletRequest request){
		HttpSession session = request.getSession();
		ServletContext sc = session.getServletContext();
		String x = sc.getRealPath("/");
		//System.out.println(x);
		return x;
	}
}
