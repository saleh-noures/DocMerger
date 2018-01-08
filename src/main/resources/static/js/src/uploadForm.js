var app = angular.module('uploadForm', ['ui.sortable','cfp.loadingBar']);

app.controller('uploadFormCntrl',function($scope, $http, $interval, cfpLoadingBar){
	
	$scope.cookieEnabled = isCookieEnabled();
	$scope.mergeRequest = {filesData:[],isFooter:false,isIndex:false};
	$scope.allowedFileUploadTypes = document.getElementById("allowedFileUploadTypes").innerText;
	$scope.maxFileSizes = document.getElementById("maxFileSizes").innerText;
	$scope.isDownloadBtnDisabled = true;
	getUploadedFilesNames();
	
///////////////////////////////////////// CLEAR EVENT HANDLER //////////////////////////////////////
	$scope.clear = function(){
		$http.delete("/clear").then(function(response){
			getUploadedFilesNames();
			$scope.isMergeBtnDisabled = false;
		    $('#errorPane').remove();
		});	
	};
	
///////////////////////////////////////// MERGE EVENT HANDLER //////////////////////////
	$scope.merge = function()
	{
		cfpLoadingBar.start();
		cfpLoadingBar.inc();
		$scope.isMergeBtnDisabled = true;
		$scope.isClearBtnDisabled = true;
		$scope.isDownloadBtnDisabled = true;
		$scope.mergeRequest.sourceFolderName =  document.getElementById("sessionIdText").innerText;
		var pdfifierUrl = document.getElementById("PDFIFIER_URL").innerText;

		// Passing a the time stamp so IE do not cache the respose result
		 var dummyDate = new Date().getTime(); 
		$http.get("/getFilesData?date="+dummyDate).then(function(response) {
	    $scope.mergeRequest.filesData = getOrderFilesObjectsArry(response.data);
	    console.log($scope.mergeRequest);
		var req = {
					method: 'POST',
					url: pdfifierUrl,
					data: $scope.mergeRequest,
					headers: {
						         'Content-Type': 'application/json',
							     'Accept': 'text/html'
						    }
				  }
				//Send the merge request
				$http(req).then(function(response)
				{
					if (response.status == '200')
					{
						//Check the merge status every second and if "Complete" will stop the interval and enable all buttons
						var status = "";
						var checkStatusInterval = $interval(function() 
								{
							        $http.get(pdfifierUrl +'/'+response.data).then(function(response)
							        		{
								                status = response.data.Status;
							                    console.log(status);
					    	                    if(status == 'Complete')
					    	                    {
							                        $scope.isMergeBtnDisabled = false;
				    			                    $scope.isClearBtnDisabled = false;
				    			                    $scope.isDownloadBtnDisabled = false;
				    			                    $interval.cancel(checkStatusInterval)
				    			                    cfpLoadingBar.complete();
					    	                     }
					    	                    else if (status.substring(0,12) == "Merge failed")
					    	                    {
							                        $scope.isMergeBtnDisabled = true;
				    			                    $scope.isClearBtnDisabled = false;
				    			                    $scope.isDownloadBtnDisabled = true;
				    			                    $interval.cancel(checkStatusInterval)
				    			                    cfpLoadingBar.complete();
				    			                    showError(status);
					    	                    	
					    	                    }
							                 });
					          }, 2000);
		    		}
				});
			 });
	}
	
	function getOrderFilesObjectsArry(UnorderFilesObjectsArry){
		
		var orderFilesObjectsArry = [];
		$('.fileName').each(function(){
			
			for(i = 0; i < UnorderFilesObjectsArry.length; i++){
				if (this.innerText.replace(/\s/g,'') == UnorderFilesObjectsArry[i].name.replace(/\s/g,'')){
					orderFilesObjectsArry.push(UnorderFilesObjectsArry[i]);
				}
				}
			
		});
		return orderFilesObjectsArry;
	}
	
////////////////// AJAX REQUEST TO GET THE UPLOADED FILES Data FORM THE SERVER //////////////////////////
	function getUploadedFilesNames(){
		// Passing a the time stamp so IE do not cache the respose result
		var dummyDate = new Date().getTime(); 
		 $http.get("/getFiles?date="+dummyDate).then(function(response) {
			  $scope.list = response.data
			  $scope.files = [];
			  if ($scope.list.length>0)
				  {
					  for(var i = 0; i< $scope.list.length; i++ )
					  {
					  $scope.files.push({'fileLink':$scope.list[i],'fileText':$scope.list[i].substring($scope.list[i].lastIndexOf("/") + 1)});
					  }
				  }

			 });
	 }
	
//////////////////Detect if Cookies are Enabled //////////////////////////	
	function isCookieEnabled() {
        var cookieEnable = navigator.cookieEnabled;

        if (typeof navigator.cookieEnabled === 'undefined' && !cookieEnable) {
            document.cookie = 'cookie-test';
            cookieEnable    = (document.cookie.indexOf('cookie-test') !== -1);
        }

        return cookieEnable;
    }
});



/////////////////////////////////////// FILE UPLOAD DIRECTIVE /////////////////////////////////////////
app.directive('filesUploadDirective', function() {
	  return {
	    restrict: 'AE',
	    replace: true,
	    //scope: {}, // isolated scope
        template: [
		   	         "<div class='input-group image-preview'>",
		                "<input type='text' class='form-control image-preview-filename' disabled='disabled' ng-model='filesnamesText'/>", 
		                "<span class='input-group-btn'>",
		                    "<div class='btn btn-default image-preview-input'>",
		                        "<span class='glyphicon glyphicon-folder-open'></span>  BROWSE",
		                        "<input type='file' name='files'  multiple='multiple' id='browseFilesBtn' accept='{{allowedFileUploadTypes}}' />",
		                    "</div>",
		                   "<button type='submit' class='btn btn-default image-preview-clear' ng-show='showUpload'>",
		                        "<span class='glyphicon glyphicon-upload'></span> UPLOAD",
		                    "</button>",
		                "</span>",
		            "</div>"
               ].join(""),
	    link: function(scope, elem, attrs) {
          $('#browseFilesBtn').change(function(){
        	  var noOfFiles =  this.files.length;
        	  var filesText =  noOfFiles + " File(s): ";
        	  var isFileSizeAllowed = true ;  
        	  //debugger;
        	  for (var i = 0; i < this.files.length; ++i) {
        		   if ((this.files[i].size/1000) > parseInt(scope.maxFileSizes))
        			   {
        			       isFileSizeAllowed = false;
        			      showError('Error: The Maximum Allowed File size is '+ parseInt(scope.maxFileSizes)/1000 +' MB!');    
        			       break;
        			   }   		
          	  }
        	  
        	  if (isFileSizeAllowed)
        		  {
	            	  for (var i = 0; i < this.files.length; ++i)
	            	  {
	              	    var name = this.files.item(i).name;
	              	    filesText = filesText + " " + name;
	              	  }
	              	  scope.$apply(function() {
	              		    	scope.filesnamesText = filesText;
	              		    	scope.showUpload = noOfFiles > 0?true:false;
	              			  });
        		  }
          });
	    }
	  };
	});

function showError(msg)
{
    $('#errorPane').remove();
	$('.container').prepend(
	                      ['<div id ="errorPane" class="row">',
	                     '<div class="col-md-2"></div>',
	                     '<div  class="col-md-8">',
		                     '<div class="alert alert-danger fade in">',
			                     '<a href="#" class="close" data-dismiss="alert">&times;</a>',
			                     '<span class="merger-text">'+ msg +'</span>',
		                     '</div>',
	                      '</div>',
	                      '<div class="col-md-2"></div>',
                       '</div>'].join("")); 
}
