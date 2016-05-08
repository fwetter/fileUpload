$(document).ready(function() {

	$(".file-dropzone").on('dragover', handleDragEnter);
	$(".file-dropzone").on('dragleave', handleDragLeave);
	$(".file-dropzone").on('drop', handleDragLeave);

	function handleDragEnter(e) {
		
		this.classList.add('drag-over');
	}

	function handleDragLeave(e) {
		
		this.classList.remove('drag-over');
	}
	
	
	function getParameterByName(name, url) {
		
	    if (!url) url = window.location.href;
	    name = name.replace(/[\[\]]/g, "\\$&");
	    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)", "i"),
	        results = regex.exec(url);
	    if (!results) return null;
	    if (!results[2]) return '';
	    return decodeURIComponent(results[2].replace(/\+/g, " "));
	}
	
	

	// "dropzoneForm" is the camel-case version of the form id "dropzone-form"
	Dropzone.options.dropzoneForm = {

		url : "upload",
		autoProcessQueue : false,
		uploadMultiple : true,
		maxFilesize : 256, // MB
		parallelUploads : 100,
		maxFiles : 100,
		addRemoveLinks : true,
		previewsContainer : ".dropzone-previews",

		// The setting up of the dropzone
		init : function() {

			var myDropzone = this;

			// first set autoProcessQueue = false
			$('#upload-button').on("click", function(e) {

				myDropzone.processQueue();
			});

			// customizing the default progress bar
			this.on("uploadprogress", function(file, progress) {

				progress = parseFloat(progress).toFixed(0);

				var progressBar = file.previewElement.getElementsByClassName("dz-upload")[0];
				progressBar.innerHTML = progress + "%";
			});

			// displaying the uploaded files information in a Bootstrap dialog
			this.on("successmultiple", function(files, serverResponse) {
				var callbackName = getParameterByName('callb');
				//
				console.log('=======upload done');
				
				//console.log('=======upload done callback',getParameterByName('callb'));
				
				if ( window.opener && (typeof window.opener.callbackUpload != 'undefined')) {
					//console.log(window.opener);
					window.opener.callbackUpload(callbackName, serverResponse);
					window.close();
				} else {
					showInformationDialog(files, serverResponse);
				}
				
				
			});
		}
	}

	function showInformationDialog(files, objectArray) {

		var responseContent = "";

		for (var i = 0; i < objectArray.length; i++) {

			var infoObject = objectArray[i];

			for ( var infoKey in infoObject) {
				if (infoObject.hasOwnProperty(infoKey)) {
					responseContent = responseContent + " " + infoKey + " -> " + infoObject[infoKey] + "<br>";
				}
			}
			responseContent = responseContent + "<hr>";
		}

		// from the library bootstrap-dialog.min.js
		BootstrapDialog.show({
			title : '<b>Server Response</b>',
			message : responseContent
		});
	}

});