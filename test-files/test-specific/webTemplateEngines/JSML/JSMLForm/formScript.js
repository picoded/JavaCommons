function showSignatureBox(targetBoxID){
		$("#signaturePad").show(0, function() {
		    
		    $("#signaturePadLine").replaceWith("<div id='signaturePadLine'></div>");
			$("#signaturePadLine").jSignature();
			
			window.jSignatureTarget = targetBoxID;
		});
	}
	
	function clearSignature(){
		$("#signaturePadLine").jSignature("reset");
	}
	
	function saveSignature(){
		var $sigdiv = $("#signaturePadLine");
		var display = $(window.jSignatureTarget);
		
		display.empty();
		
		// Getting signature as SVG and rendering the SVG within the browser. 
		// (!!! inline SVG rendering from IMG element does not work in all browsers !!!)
		// this export plugin returns an array of [mimetype, base64-encoded string of SVG of the signature strokes]
		var datapair = $sigdiv.jSignature("getData", "svgbase64") ;
		var i = new Image();
		i.src = "data:" + datapair[0] + "," + datapair[1] ;
		$(i).height(80);
		$(i).appendTo(display); // append the image (SVG) to DOM.
						  
		//$("#signaturePadLine").jSignature("reset");
		$("#signaturePad").hide();
	}
	
	        function getAllDomValues(){
            var allDomsArray = document.getElementsByTagName("*");
            var newUrl = window.location.href + "/save";
            for(var i = 0; i < allDomsArray.length; ++i){
                var elementName = allDomsArray[i].name;
                if(typeof elementName !== 'undefined'){
                    var elementValue = allDomsArray[i].value;
                    if(typeof elementValue !== 'undefined'){
                        newUrl = updateURLParameter(newUrl, elementName, elementValue);
                        
                    }
                }
            }
			//window.location.href = newUrl;
        }
        
        <!--basic page clearing function which doesnt rely on form tag-->
        function clearPageValues(){
            var allInputDoms = document.getElementsByTagName('input');
            for(var i = 0; i < allInputDoms.length; ++i){
                var inputType = allInputDoms[i].type;
                switch(inputType){
                    case 'text': allInputDoms[i].value = '';
                }
            }
            
            var allSelectDoms = document.getElementsByTagName('select');
            for(var i = 0; i < allSelectDoms.length; ++i){
                allSelectDoms[i].selectedIndex = 0;
            }
			
			window.location.href = window.location.href + "/reset";
        }
        
        <!--taken from http://stackoverflow.com/questions/1090948/change-url-parameters/10997390#10997390 -->
        function updateURLParameter(url, param, paramVal){
            var TheAnchor = null;
            var newAdditionalURL = "";
            var tempArray = url.split("?");
            var baseURL = tempArray[0];
            var additionalURL = tempArray[1];
            var temp = "";
        
            if (additionalURL) 
            {
                var tmpAnchor = additionalURL.split("#");
                var TheParams = tmpAnchor[0];
                    TheAnchor = tmpAnchor[1];
                if(TheAnchor)
                    additionalURL = TheParams;
        
                tempArray = additionalURL.split("&");
        
                for (i=0; i<tempArray.length; i++)
                {
                    if(tempArray[i].split('=')[0] != param)
                    {
                        newAdditionalURL += temp + tempArray[i];
                        temp = "&";
                    }
                }        
            }
            else
            {
                var tmpAnchor = baseURL.split("#");
                var TheParams = tmpAnchor[0];
                    TheAnchor  = tmpAnchor[1];
        
                if(TheParams)
                    baseURL = TheParams;
            }
        
            if(TheAnchor)
                paramVal += "#" + TheAnchor;
        
            var rows_txt = temp + "" + param + "=" + paramVal;
            return baseURL + "?" + newAdditionalURL + rows_txt;
        }