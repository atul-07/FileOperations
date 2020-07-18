'use strict';

var singleUploadForm = document.querySelector('#singleUploadForm');
var singleFileUploadInput = document.querySelector('#singleFileUploadInput');
var singleFileUploadError = document.querySelector('#singleFileUploadError');
var singleFileUploadSuccess = document.querySelector('#singleFileUploadSuccess');
var showAllFilesId = document.querySelector('#showAllFilesId');
var allDirectoriesId = document.querySelector('#allDirectoriesId');

var multipleUploadForm = document.querySelector('#multipleUploadForm');
var multipleFileUploadInput = document.querySelector('#multipleFileUploadInput');
var multipleFileUploadError = document.querySelector('#multipleFileUploadError');
var multipleFileUploadSuccess = document.querySelector('#multipleFileUploadSuccess');
var createDirectoryFormInput = document.querySelector("#createDirectoryFormInput");
var moveAFileResult  = document.querySelector("#moveAFileResult");
var allDirectoriesInModalId= document.querySelector("#allDirectoriesInModalId");
var searchAKeywordDiv= document.querySelector("#searchAKeywordDiv");
var currentDirectory = document.querySelector("#currentDirectory");
var currentDirectoryInputId = document.querySelector("#currentDirectoryInputId");






function uploadSingleFile(file,currentDirectory) {
	
    var formData = new FormData();
    formData.append("file", file);
    formData.append("currentDirectory", currentDirectory);
    
    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadFile");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        
        if(xhr.status == 200) {
            singleFileUploadError.style.display = "none";
            singleFileUploadSuccess.innerHTML = "<p>"+response["result"]+"</p>";
            singleFileUploadSuccess.style.display = "block";
            showAllFilesOfADirectory(currentDirectory);
        } else {
            singleFileUploadSuccess.style.display = "none";
            singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
    
}
/*
function uploadMultipleFiles(files) {
    var formData = new FormData();
    for(var index = 0; index < files.length; index++) {
        formData.append("files", files[index]);
    }

    var xhr = new XMLHttpRequest();
    xhr.open("POST", "/uploadMultipleFiles");

    xhr.onload = function() {
        console.log(xhr.responseText);
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
            multipleFileUploadError.style.display = "none";
            var content = "<p>All Files Uploaded Successfully</p>";
            for(var i = 0; i < response.length; i++) {
                content += "<p>DownloadUrl : <a href='" + response[i].fileDownloadUri + "' target='_blank'>" + response[i].fileDownloadUri + "</a></p>";
            }
            multipleFileUploadSuccess.innerHTML = content;
            multipleFileUploadSuccess.style.display = "block";
        } else {
            multipleFileUploadSuccess.style.display = "none";
            multipleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
        }
    }

    xhr.send(formData);
}
*/

singleUploadForm.addEventListener('submit', function(event){
    var files = singleFileUploadInput.files;
    var currentDirectory = document.getElementById("currentDirectoryInputId").value;
    if(files.length === 0) {
        singleFileUploadError.innerHTML = "Please select a file";
        singleFileUploadError.style.display = "block";
    }
    uploadSingleFile(files[0],currentDirectory);
    event.preventDefault();
}, true);


/*
multipleUploadForm.addEventListener('submit', function(event){
    var files = multipleFileUploadInput.files;
    if(files.length === 0) {
        multipleFileUploadError.innerHTML = "Please select at least one file";
        multipleFileUploadError.style.display = "block";
    }
    uploadMultipleFiles(files);
    event.preventDefault();
}, true);

*/

function showAllFiles() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/searchAllFiles");

    xhr.onload = function() {
        
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
        
        	var res ="";
        	for(var i = 0; i < response.length; i++) {
        	    var obj = response[i];
        	    res += obj +"<br>";
        	}
        	showAllFilesId.innerHTML = ""+res;
            
        } else {
        }
    }

    xhr.send();
}

function showAllDirectories() {
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/showAllDirectories");

    xhr.onload = function() {
        
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
        	var res ="";
        	for(var i = 0; i < response.length; i++) {
        	    var obj = response[i];
        	    res += "<a href='javascript:showAllFilesOfADirectory(\""+obj+"\")'>"+obj+"</a><br>";
        	}
        	
        	allDirectoriesId.innerHTML = ""+res;
        	allDirectoriesId.style.display="block";
        	showAllFilesId.style.display="none";
            
        } else {
        }
    }

    xhr.send();
}

function createADirectory() {
	
	var directoryName = document.getElementById("createDirectoryFormInput").value;
	
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/createADirectory?directoryName="+directoryName);

    xhr.onload = function() {
        
        var response = JSON.parse(xhr.responseText);
        
        if(xhr.status == 200) {
        	showAllDirectories();
        	createDirectoryFormInput.value="";
            
        } else {
        }
    }

    xhr.send();
}

function showAllFilesOfADirectory(directoryName) {
	
    var xhr = new XMLHttpRequest();
    xhr.open("GET", "/showAllFilesOfADirectory?directoryName="+directoryName);

    xhr.onload = function() {
        
        var response = JSON.parse(xhr.responseText);
        if(xhr.status == 200) {
        
        	var res ="";
        	//directoryNameTmp =directoryName.replace(" ","+");
        	
        	for(var i = 0; i < response.length; i++) {
        		
        	    var obj =  response[i].replace("\\","");
        	    var fileNameTmp = obj.replace(" ","+");

        	    res += "<input type='button' value='rename' onClick=renameButtonClick('"+obj+"') />" +
        	    		"<input type='button' value='move or copy' onClick=openModal('"+fileNameTmp+"') /><input type='button' value='delete' onClick=deleteAFile(\""+directoryName+"\",\""+fileNameTmp+"\") />" +
        	    		"&nbsp;&nbsp;"+obj+"<span id='"+obj+"SpanId' style='display:none'><input id='"+obj+"TextBoxId' type='text' value='"+obj+"' />" +
        	    				"<input type='button' value='save' onClick='renameSaveButtonClick(\""+obj+"\")'/></span><br>" ;
        	}
        	showAllFilesId.innerHTML = ""+res;
        	allDirectoriesId.style.display="none";
        	showAllFilesId.style.display="block";
        	currentDirectory.innerHTML=directoryName;
        	currentDirectoryInputId.value = directoryName;
            
        } else {
        }
    }

    xhr.send();
}
function renameButtonClick(fileName){
	document.getElementById(fileName+"SpanId").style.display = 'block';
}
function renameSaveButtonClick(fileName){
	
  var oldFileName = fileName;
  var newFileName = document.getElementById(fileName+"TextBoxId").value;
  var currentDirectory = document.getElementById("currentDirectory").innerHTML;
  
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/renameAFile?currentDirectory="+currentDirectory+"&oldFileName="+oldFileName+"&newFileName="+newFileName);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        	alert(response);
	          	showAllFilesOfADirectory(currentDirectory);
	        } else {
	        
	        }
	    }

	    xhr.send();
}


function deleteAFile(directoryName,fileName){
	
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/deleteAFile?fileName="+directoryName+"/"+fileName);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        
	        	var res ="";
	        	
	        	allDirectoriesId.style.display="none";
	        	showAllFilesId.style.display="block";
	        	currentDirectory.innerHTML=directoryName;
	        	currentDirectoryInputId.value = directoryName;
	        	showAllFilesOfADirectory(directoryName);
	            
	        } else {
	        }
	    }

	    xhr.send();
}
function openModal(fileName){
	    var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/showAllDirectories");

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        	var res ="";
	        	for(var i = 0; i < response.length; i++) {
	        	    var obj = response[i];
	        	    res += "<input type='radio' name='directoryInWhichFileWillBePastedName' value='"+obj+","+fileName+"' />" +
	        	    		""+obj+"</a><br>";
	        	}
	        	
	        	allDirectoriesInModalId.innerHTML = ""+res;
	        	allDirectoriesInModalId.style.display="block";
	            
	        } else {
	        }
	    }

	    xhr.send();
	
	
	var modal = document.getElementById("myModal");
    var btn = document.getElementById("myBtn");
    var span = document.getElementsByClassName("close")[0];
   
  //  modal.style.display = "block";
    $("#myModal").fadeIn("slow");
    
   

    span.onclick = function() {
      modal.style.display = "none";
    }

    window.onclick = function(event) {
      if (event.target == modal) {
        modal.style.display = "none";
      }
    }
}

function moveAFile(){
	var targetDirectory = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[0];
	var fileName = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[1];
	var sourceDirectory = document.getElementById('currentDirectoryInputId').value;
	
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/moveAFile?sourceDirectory="+sourceDirectory+"&targetDirectory="+targetDirectory+"&fileName="+fileName);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        
	        	if(response=='successfully moved'){
	        		moveAFileResult.innerHTML = "<b style='color:green'>"+response+"</b>";
	        	}
	        	else{
	        		moveAFileResult.innerHTML = "<b style='color:red'>Not able to move, may be a file with same name alredy exists</b>";
	        	}
	        	
	        	allDirectoriesInModalId.style.display="block";
	        	showAllFilesOfADirectory(sourceDirectory);
	        	$("#myModal").fadeOut(2000);
	            
	        } else {
	        	moveAFileResult.innerHTML = "<b style='color:red'>Not able to move, may be a file with same name alredy exists</b>";
	        }
	    }

	    xhr.send();
	
}

function searchAKeyword(){
	
	var keyword = document.getElementById("searchKeywordFormInput").value;
	
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/searchAKeyword?keyword="+keyword);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        	var res ="";
	        	for(var i = 0; i < response.length; i++) {
	        	    var obj = response[i];
	        	    res += obj+"<br>";
	        	}
	        	
	        	searchAKeywordDiv.innerHTML= res;
	        	
	            
	        } else {
	        }
	    }

	    xhr.send();
}

//--- I am here 

function copyAFile(){
	var targetDirectory = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[0];
	var fileName = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[1];
	var sourceDirectory = document.getElementById('currentDirectoryInputId').value;
	
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/copyAFile?sourceDirectory="+sourceDirectory+"&targetDirectory="+targetDirectory+"&fileName="+fileName);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        
	        	if(response=='successfully copied'){
	        		moveAFileResult.innerHTML = "<b style='color:green'>"+response+"</b>";
	        	}
	        	else{
	        		moveAFileResult.innerHTML = "<b style='color:red'>"+response+"</b>";
	        	}
	        	
	        	allDirectoriesInModalId.style.display="block";
	        	showAllFilesOfADirectory(sourceDirectory);
	        	$("#myModal").fadeOut(2000);
	        //	showAllFilesId.style.display="none";
	            
	        } else {
	        	document.getElementById("moveAFileResult").innerHTML = "<b style='color:red'>"+response+"</b>";
	           // singleFileUploadSuccess.style.display = "none";
	            //singleFileUploadError.innerHTML = (response && response.message) || "Some Error Occurred";
	        }
	    }

	    xhr.send();
	
}
function renameAFile(){
	var targetDirectory = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[0];
	var fileName = document.querySelector('input[name="directoryInWhichFileWillBePastedName"]:checked').value.split(",")[1];
	var sourceDirectory = document.getElementById('currentDirectoryInputId').value;
	
	 var xhr = new XMLHttpRequest();
	    xhr.open("GET", "/moveAFile?sourceDirectory="+sourceDirectory+"&targetDirectory="+targetDirectory+"&fileName="+fileName);

	    xhr.onload = function() {
	        
	        var response = JSON.parse(xhr.responseText);
	        if(xhr.status == 200) {
	        
	        	if(response=='successfully moved'){
	        		moveAFileResult.innerHTML = "<b style='color:green'>"+response+"</b>";
	        	}
	        	else{
	        		moveAFileResult.innerHTML = "<b style='color:red'>Not able to move, may be a file with same name alredy exists</b>";
	        	}
	        	
	        	allDirectoriesInModalId.style.display="block";
	        	showAllFilesOfADirectory(sourceDirectory);
	        	$("#myModal").fadeOut(2000);
	            
	        } else {
	        	moveAFileResult.innerHTML = "<b style='color:red'>Not able to move, may be a file with same name alredy exists</b>";
	        }
	    }

	    xhr.send();
	
}