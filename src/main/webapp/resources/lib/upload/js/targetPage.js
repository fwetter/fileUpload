
/* ======== upload */

/*
 * esempio div  html preview
 * 
 * <div id="photoGalleryPreview" data-target-id="photoGallery" class="uploadPreview"></div>
 * 
 * ID della textarea e ID del div devono avere lo stesso prefisso, quello del div deve finire con 'Preview'
 * 
 * inserire textarea con id={data-target-id} e class uploadData
 * 
 * <textarea name="photoGallery" id="photoGallery" cssClass="uploadData" >
 */

	function callbackUpload(callb, data){
		
		//console.log('callbackUpload ' + callb,  data);
		showUploadedFiles(callb, data);
		
	}
	
	function showUploadedFiles(callb, data){
		
		var $targetPreview = $('#' + callb + "Preview"), 
		$img;
		//console.log('showUploadedFiles ' + callb,  data);
		//console.log('$targetPreview', $targetPreview);
		
		$.each(data, function( index, value ) {
			
				//fileType:"image/jpg"
				if(value.fileType && value.fileType.indexOf('image')>-1){
					$img = $('<img>',{ 
						src : value.fileUrl, 
						class : 'img-thumbnail attachment',
						'data-url' : value.fileUrl});
				}
			
			  //console.log( index + ": " + value.fileUrl );
				
				// aggiungo il file solo se non e' gia' inserito nel di preview
				if(!getPreviewFileByDataUrl($targetPreview, value.fileUrl)){
					$targetPreview.append($img);
				};
				
			  
			});
		
		updateUploadField(callb);
		
	}
	
	function getPreviewFileByDataUrl($container, data_url){

		var $item;

		if($container && $container.length > 0){
			
			$item = $container.find("[data-url='" + data_url + "']");
			
			if($item.length>0){
				//console.log('found item', data_url);
				return $item;
			}
			
		}
		
		return null;
		
	}
	
	function updateUploadField(targetId){
		
		var $sourceElem, $target, data_url, value_arr=[], sep = ';\n';
		
		$sourceElem = $('#' + targetId + 'Preview');
		$target = $('#' + targetId);
		
		//console.log($target.val().split(sep));
		
		$sourceElem.find('.attachment').each(function( index ) {
			
			data_url = $( this ).data('url');
			if(data_url) value_arr.push(data_url);				
			//console.log( index + ": " + $( this ).data('url'));
			
		});
		
		$target.val(value_arr.join(sep));
		
	}
	
	function endsWith(str, suffix) {
	    
		return str.slice(-suffix.length) === suffix;
		
	}
	
	function getFileExtension(str){
		
		var pos, ext ="";
		if(!str){
			return ext;
		}
		
		pos = str.indexOf(".");
		if(pos > -1){
			ext =  str.substring(str.lastIndexOf(".")+1, str.length);
		}
		return ext;
		
		
	}
	
	function getFileType(url){

		var fileType = null;
		if(!url){
			return fileType;
		}
		url=url.toLowerCase();
		
		if(endsWith(url, 'jpg') || endsWith(url, 'jpeg') || endsWith(url, 'gif') || endsWith(url, 'png')){
			fileType = "image/" + getFileExtension(url);
		}

		return fileType;
		
	}
	
	var uploadDataSep =  ';\n'
	
	
$(document).ready(function() {	
	
	$(".uploadPreview").sortable();
	$(".uploadPreview").disableSelection();
	
	$(".uploadPreview").on('sortstop', function(event, ui) {
		//console.log( $(this).data('target-id'));
		updateUploadField($(this).data('target-id'));
	});
	
	$('.uploadData').each(function( index ) {
		var $elem = $(this),
		uploadDataArr = $elem.val().split(uploadDataSep),
		item, fileType,
		data = [];
		
		$.each(uploadDataArr, function( i, value ) {

			item = {};
			item['fileUrl']= value;
			fileType = getFileType(value);//endsWith(value.toLowerCase(), 'jpg') ? "image/jpg" : null;
			item['fileType']= fileType;
			data.push(item);
			//console.log( item);
			  
		});
		
		showUploadedFiles($elem.attr('id'), data);

		  
	});
	
	
	
});
