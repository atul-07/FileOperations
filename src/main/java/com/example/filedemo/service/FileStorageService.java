package com.example.filedemo.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.filedemo.exception.FileStorageException;
import com.example.filedemo.property.FileStorageProperties;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;
    private static final long MAX_SIZE_IN_BYTES = 134303;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
                .toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file,String currentDirectory) {
        // Normalize file name
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());

        try {
            // Check if the file's name contains invalid characters
            if(fileName.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileName);
            }
            int flag =-1;
            if(fileName.endsWith(".doc") || fileName.endsWith(".docx") || fileName.endsWith(".pdf") || fileName.endsWith(".txt") || fileName.endsWith(".html")){
            	flag =0;
            }

            if(flag==-1){
            	throw new FileStorageException("This file extension does not support, " + fileName + ". Please try again!");
            }
            // Copy file to the target location (Replacing existing file with the same name)
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        	String userName = auth.getName();
        	
        	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
        	
        	if(!Files.exists(Paths.get(parentStorageDir))){
        		new File(parentStorageDir).mkdirs();
        	}
        	
        	parentStorageDir = parentStorageDir+"\\";
        	
        	long userDirectorySize = FileUtils.sizeOfDirectory(new File(parentStorageDir));
        	long fileSize = file.getSize();
        	if(userDirectorySize+fileSize > MAX_SIZE_IN_BYTES){
        		 throw new FileStorageException("Storage limit exceeded.");
        	}
        	
            String fileStorageLocationString = parentStorageDir+currentDirectory;
            
            Path targetLocation = Paths.get(fileStorageLocationString).resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation);
            

            return fileName;
        } catch (Exception ex) {
        	System.out.println("---in exception 1");
            throw new FileStorageException("Could not store file " + fileName + "."+ ex.getMessage(), ex);
        }
    }
    
    
/*
    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if(resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }*/
    
    
    public String deleteFile(String fileName) {
        
        try {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        	String userName = auth.getName();
        	
        	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
        	
        	if(!Files.exists(Paths.get(parentStorageDir))){
        		new File(parentStorageDir).mkdirs();
        	}
        	
        	parentStorageDir = parentStorageDir+"\\";
        	
            Path path = Paths.get(parentStorageDir+fileName);
            Files.delete(path); 

            return "File deleted successfully";
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName + ". Please try again!", ex);
        }
    }
    
 public ArrayList<String> searchFileNames(String fileName) {
        
        try {
        	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        	String userName = auth.getName();
        	
        	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
        	
        	if(!Files.exists(Paths.get(parentStorageDir))){
        		new File(parentStorageDir).mkdirs();
        	}
        	
        	parentStorageDir = parentStorageDir+"\\";
            ArrayList<String> fileNamesList = new ArrayList<String>();
            
            Files.list(Paths.get(parentStorageDir)).forEach(a->fileNamesList.add(a.toString()));
            
            ArrayList<String> fileNamesListOnlyNames = new ArrayList<String>();
            for(String s: fileNamesList){
            	String tmp = s.replace(parentStorageDir, "");

            	if(tmp.contains(fileName))
            	fileNamesListOnlyNames.add(s);
            }
            return fileNamesListOnlyNames;
            
            
        } catch (Exception ex) {
            throw new FileStorageException("File not found " + fileName + ". Please try again!", ex);
        }
    }
 
 public ArrayList<String> searchAllFiles() {
     
     try {
    	 
     	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
     	String userName = auth.getName();
     	
     	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
     	
     	if(!Files.exists(Paths.get(parentStorageDir))){
     		new File(parentStorageDir).mkdirs();
     	}
     	
     	parentStorageDir = parentStorageDir+"\\";
         ArrayList<String> fileNamesList = new ArrayList<String>();
         
         Files.list(Paths.get(parentStorageDir)).forEach(a->fileNamesList.add(a.toString()));
         
         ArrayList<String> fileNamesListOnlyNames = new ArrayList<String>();
         for(String s: fileNamesList){
         	String tmp = s.replace(parentStorageDir, "");

         //	if(tmp.contains(fileName))
         	fileNamesListOnlyNames.add(tmp);
         }
         
         return fileNamesListOnlyNames;
         
         
     } catch (Exception ex) {
         throw new FileStorageException("File not found, Please try again!", ex);
     }
 }


public ArrayList<String> currentDirectory() {
    
    try {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();
    	
    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
    	
    	if(!Files.exists(Paths.get(parentStorageDir))){
    		new File(parentStorageDir).mkdirs();
    	}
    	
    	parentStorageDir = parentStorageDir+"\\";
    	
    	
        ArrayList<String> fileNamesList = new ArrayList<String>();
        
        Files.list(Paths.get(parentStorageDir)).forEach(a->fileNamesList.add(a.toString()));
        
        ArrayList<String> fileNamesListOnlyNames = new ArrayList<String>();
        for(String s: fileNamesList){
        	String tmp = s.replace(parentStorageDir, "");
        	fileNamesListOnlyNames.add(tmp);
        }
        
        return fileNamesListOnlyNames;
        
        
    } catch (Exception ex) {
        throw new FileStorageException("Directory not found, Please try again!", ex);
    }
}
public ArrayList<String> showAllDirectories() {
    
    try {
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();
    	
    	String parentStorageDir =  fileStorageLocation.toString()+"\\";
    	
    	if(!Files.exists(Paths.get(parentStorageDir+userName))){
    		new File(parentStorageDir+userName).mkdirs();
    	}
    	parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
    	
        ArrayList<String> fileNamesList = new ArrayList<String>();
        
        Files.list(Paths.get(parentStorageDir)).filter(a-> Files.isDirectory(a)).forEach(a->fileNamesList.add(a.toString()));
        
        ArrayList<String> fileNamesListOnlyNames = new ArrayList<String>();
        for(String s: fileNamesList){
        	String tmp = s.replace(parentStorageDir, "").replace("\\", "");
        	fileNamesListOnlyNames.add(tmp);
        }
        
        return fileNamesListOnlyNames;
        
        
    } catch (Exception ex) {
        throw new FileStorageException("Directory not found, Please try again!", ex);
    }
}


public String createADirectory(String directoryName) {
    
    try {
    	
    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    	String userName = auth.getName();
    	
    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
    	
    	if(!Files.exists(Paths.get(parentStorageDir))){
    		new File(parentStorageDir).mkdirs();
    	}
    	
    	parentStorageDir = parentStorageDir+"\\";
        
    	boolean result = new File(parentStorageDir+directoryName).mkdir();
    	if(result)
    		return "Directory successfully created.";
    	else{
    		System.out.println("Directory not able to create");
    		return "Directory not able to create.";
    	}
        
        
        
    } catch (Exception ex) {
        throw new FileStorageException("Directory not found, Please try again!", ex);
    }
}

public ArrayList<String>  showAllFilesOfADirectory(String directoryName) {
    
	 try {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	
	    	parentStorageDir = parentStorageDir+"\\";
	    	
     	 parentStorageDir =  parentStorageDir+directoryName;
         ArrayList<String> fileNamesList = new ArrayList<String>();
         
         Files.list(Paths.get(parentStorageDir)).forEach(a->fileNamesList.add(a.toString()));
         
         ArrayList<String> fileNamesListOnlyNames = new ArrayList<String>();
         for(String s: fileNamesList){
         	String tmp = s.replace(parentStorageDir, "");

         //	if(tmp.contains(fileName))
         	fileNamesListOnlyNames.add(tmp);
         }
         
         return fileNamesListOnlyNames;
         
         
     } catch (Exception ex) {
         throw new FileStorageException("File not found, Please try again!", ex);
     }
}

public ArrayList<String>  deleteAFile(String fileName) {
    // atul here need to take input for current directory
	 try {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	
	    	parentStorageDir = parentStorageDir+"\\";
		 
        Path path = Paths.get( parentStorageDir+fileName);
    	
    	Files.delete(path);
        ArrayList<String> result = new ArrayList<String>();
        result.add("successfully deleted");
                
        return result;
        
        
    } catch (Exception ex) {
        throw new FileStorageException("File not able to delete, Please try again!", ex);
    }
}


public ArrayList<String> moveAFile(String sourceDirectory,String targetDirectory,String fileName) {
    
	 try {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	
	    	parentStorageDir = parentStorageDir+"\\";
       
       Path temp = Files.move 
    	        (Paths.get(parentStorageDir+sourceDirectory+"\\"+fileName),  
    	        Paths.get(parentStorageDir+targetDirectory+"\\"+fileName)); 
   	
   	   System.out.println(temp.toString());
       ArrayList<String> result = new ArrayList<String>();
       result.add("successfully moved");
               
       return result;
       
       
   } catch (Exception ex) {
       throw new FileStorageException("File not able to move, Please try again!", ex);
   }
}
public ArrayList<String> copyAFile(String sourceDirectory,String targetDirectory,String fileName) {
    
	 try {
		 
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	parentStorageDir = parentStorageDir+"\\";
	    	
        	long userDirectorySize = FileUtils.sizeOfDirectory(new File(parentStorageDir));
        	long fileSize = new File(parentStorageDir+sourceDirectory+"\\"+fileName).length();
        	if(userDirectorySize+fileSize > MAX_SIZE_IN_BYTES){
        		 throw new FileStorageException("Storage limit exceeded.");
        	}

		 
      
      Path temp = Files.copy
   	        (Paths.get(parentStorageDir+sourceDirectory+"\\"+fileName),  
   	        Paths.get(parentStorageDir+targetDirectory+"\\"+fileName)); 
  	
  	  System.out.println(temp.toString());
      ArrayList<String> result = new ArrayList<String>();
      result.add("successfully copied");
              
      return result;
      
      
  } catch (Exception ex) {
      throw new FileStorageException("File not able to copy. "+ex.getMessage(), ex);
  }
}


public ArrayList<String>  renameAFile(String currentDirectory,String oldFileName,String newFileName) {
    
	System.out.println(currentDirectory);
	 try {
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	
	    	parentStorageDir = parentStorageDir+"\\";
		 
		File f1 = new File(parentStorageDir+currentDirectory+"/"+oldFileName);
		File f2 = new File(parentStorageDir+currentDirectory+"/"+newFileName);
		 f1.renameTo(f2);
		 
      ArrayList<String> result = new ArrayList<String>();
      result.add("successfully renamed");
              
      return result;
      
      
  } catch (Exception ex) {
      throw new FileStorageException("File not able to move, Please try again!", ex);
  }
}


public ArrayList<String> searchAKeyword(String keyword) {
    
	 try {
			ArrayList<String> filesList = new ArrayList<String>();
			
	    	Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    	String userName = auth.getName();
	    	
	    	String parentStorageDir =  fileStorageLocation.toString()+"\\"+userName;
	    	
	    	if(!Files.exists(Paths.get(parentStorageDir))){
	    		new File(parentStorageDir).mkdirs();
	    	}
	    	
	    	parentStorageDir = parentStorageDir+"\\";
			
			listf(parentStorageDir, keyword, filesList);
       return filesList;
       
       
   } catch (Exception ex) {
       throw new FileStorageException("File not able to delete, Please try again!", ex);
   }
}



public void listf(String directoryName, String keyword, ArrayList<String> fileNames) {

	File directory = new File(directoryName);
	File[] fList = directory.listFiles();

	for (File file : fList) {
		if (file.isFile()) {
			if (file.getAbsolutePath().endsWith(".txt") || file.getAbsolutePath().endsWith(".html")) {
				try {
					Scanner scanner = new Scanner(file);
					String currentLine;

					while ((currentLine = scanner.nextLine()) != null) {
						if (currentLine.indexOf(keyword) != -1) {

							String s = file.getAbsolutePath().replace(fileStorageLocation.toString(),"");
							fileNames.add(s);
						}
					}
					scanner.close();
				} catch (Exception ex) {
				}
			}
			else if (file.getAbsolutePath().endsWith(".pdf")) {
				try {
					
			        try (PDDocument doc = PDDocument.load(file)) {

			            PDFTextStripper stripper = new PDFTextStripper();
			            String text = stripper.getText(doc);
			            if(text.contains(keyword)){
			            	String s = file.getAbsolutePath().replace(fileStorageLocation.toString(),"");
							fileNames.add(s);
			            }
			            
			        }
			        catch(Exception ex1){}
							
				} catch (Exception ex) {
				}
			}
			else if (file.getAbsolutePath().endsWith(".doc") || file.getAbsolutePath().endsWith(".docx") ) {
				try {
					   FileInputStream fis = new FileInputStream(file);
					   XWPFDocument xdoc = new XWPFDocument(OPCPackage.open(fis));
					   XWPFWordExtractor extractor = new XWPFWordExtractor(xdoc);
					   if(extractor.getText().contains(keyword)){
						   String s = file.getAbsolutePath().replace(fileStorageLocation.toString(),"");
							fileNames.add(s);
					   }
					} catch(Exception ex) {
					    ex.printStackTrace();
					}
			}
			
		} else if (file.isDirectory()) {
			listf(file.getAbsolutePath(), keyword, fileNames);
			
		}
	}
}

}
