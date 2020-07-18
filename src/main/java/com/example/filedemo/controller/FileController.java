package com.example.filedemo.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.filedemo.payload.UploadFileResponse;
import com.example.filedemo.service.FileStorageService;

@RestController
public class FileController {

	private static final Logger logger = LoggerFactory.getLogger(FileController.class);

	@Autowired
	private FileStorageService fileStorageService;

    @RequestMapping(value = "/", method = RequestMethod.GET)  
    public HashMap<String,String> index() {  
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();
    	
    	HashMap<String,String> hm = new HashMap<String,String>();
    	hm.put("userName", userName);
		return hm;
    }  

    /*
	@PostMapping("/uploadFile")
	public UploadFileResponse uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("currentDirectory") String currentDirectory) {
		String fileName = fileStorageService.storeFile(file, currentDirectory);

		String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/")
				.path(fileName).toUriString();

		return new UploadFileResponse(fileName, fileDownloadUri, file.getContentType(), file.getSize());
	}
	*/
    
	@PostMapping("/uploadFile")
	public HashMap<String,String> uploadFile(@RequestParam("file") MultipartFile file,
			@RequestParam("currentDirectory") String currentDirectory) {
		HashMap<String,String> hm = new HashMap<String,String>();
		try{
		String fileName = fileStorageService.storeFile(file, currentDirectory);
		hm.put("result","successfully stored" );
		}
		catch(Exception ex){
			System.out.println("---in exception 2");
			hm.put("result",ex.getMessage());
		}
      return hm;
	}

	@GetMapping("/deleteFile")
	public HashMap<String,String> deleteFile(@RequestParam("fileName") String fileName) {
		HashMap<String,String> hm = new HashMap<String,String>();
		try{
		fileStorageService.deleteFile(fileName);
		hm.put("result",fileName +" successfully deleted" );
		}catch(Exception ex){
		hm.put("result",fileName +" not able to be deleted" );
		}
		
		return hm;
	}

	@GetMapping("/searchFiles")
	public UploadFileResponse searchFiles(@RequestParam("fileName") String fileName) {
		ArrayList<String> result = fileStorageService.searchFileNames(fileName);

		for (String s : result) {

			String fileDownloadUri = ServletUriComponentsBuilder.fromCurrentContextPath().path("/downloadFile/").path(s)
					.toUriString();

			return new UploadFileResponse(s, fileDownloadUri, null, 0);

		}
		return null;
	}

	@GetMapping("/searchAllFiles")
	public List<String> searchAllFiles() {
		ArrayList<String> result = fileStorageService.searchAllFiles();
		return result;

	}

	@GetMapping("/showAllDirectories")
	public List<String> currentDirectory() {
		ArrayList<String> result = fileStorageService.showAllDirectories();
		return result;

	}

	@GetMapping("/createADirectory")
	public ArrayList<String> createADirectory(@RequestParam("directoryName") String directoryName) {
		String output = fileStorageService.createADirectory(directoryName);
		ArrayList<String> result = new ArrayList<String>();
		result.add(output);
		return result;

	}

	@GetMapping("/showAllFilesOfADirectory")
	public ArrayList<String> showAllFilesOfADirectory(@RequestParam("directoryName") String directoryName) {
		ArrayList<String> result = fileStorageService.showAllFilesOfADirectory(directoryName);
		return result;

	}

	@GetMapping("/deleteAFile")
	public ArrayList<String> deleteAFile(@RequestParam("fileName") String fileName) {
		ArrayList<String> result = fileStorageService.deleteAFile(fileName);
		return result;

	}

	@GetMapping("/moveAFile")
	public ArrayList<String> moveAFile(@RequestParam("sourceDirectory") String sourceDirectory,
			@RequestParam("targetDirectory") String targetDirectory, @RequestParam("fileName") String fileName) {

		ArrayList<String> result = fileStorageService.moveAFile(sourceDirectory, targetDirectory, fileName);
		return result;

	}

	@GetMapping("/copyAFile")
	public ArrayList<String> copyAFile(@RequestParam("sourceDirectory") String sourceDirectory,
			@RequestParam("targetDirectory") String targetDirectory, @RequestParam("fileName") String fileName) {
		ArrayList<String> result = new ArrayList<String>();
		try{
		result.addAll(fileStorageService.copyAFile(sourceDirectory, targetDirectory, fileName));
		}
		catch(Exception ex){
			result.add(ex.getMessage());
		}
		
		return result;

	}

	@GetMapping("/searchAKeyword")
	public ArrayList<String> searchAKeyword(@RequestParam("keyword") String keyword) {
		ArrayList<String> result = fileStorageService.searchAKeyword(keyword);
		return result;

	}

	@GetMapping("/renameAFile")
	public ArrayList<String> renameAFile(@RequestParam("currentDirectory") String currentDirectory,
			@RequestParam("oldFileName") String oldFileName, @RequestParam("newFileName") String newFileName) {

		System.out.println(currentDirectory);
		ArrayList<String> result = fileStorageService.renameAFile(currentDirectory, oldFileName, newFileName);
		return result;

	}


	// -------------- --------------------------- ------------------

/*
	@GetMapping("/downloadFile/{fileName:.+}")
	public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
		// Load file as Resource
		Resource resource = fileStorageService.loadFileAsResource(fileName);

		// Try to determine file's content type
		String contentType = null;
		try {
			contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
		} catch (IOException ex) {
			logger.info("Could not determine file type.");
		}

		// Fallback to the default content type if type could not be determined
		if (contentType == null) {
			contentType = "application/octet-stream";
		}

		return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
				.body(resource);
	}
	*/

}
