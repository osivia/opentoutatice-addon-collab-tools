var lang = 'fr';

function createTinyMceExtractBar(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddEditor', false, textareas[index].id);
	}
}

function createTinyMceExtract() {

	tinyMCE
			.init({
				height: "150",
				mode : "specific_textareas",
				language : "fr",
				theme : "modern",
				editor_selector : "mceExtract",
				editor_deselector : "disableMCEInit",
				plugins : ["nuxeolink link textcolor"],
				content_css: "/nuxeo/css/toutatice-popup.min.css?" + new Date().getTime(),

				valid_elements: "strong/b,em,sub,u,p[style],ol,ul,li,blockquote,span[style],a[href]",
				valid_styles: {
					'p': 'text-align,padding-left',
					'span': 'text-decoration,color,background-color'
				},
				
				relative_urls : true,
				document_base_url : baseURL,

				// Theme options
				toolbar1 : "bold italic underline | strikethrough subscript | alignleft aligncenter alignright alignjustify | bullist numlist | outdent indent blockquote | forecolor backcolor | nuxeolink link unlink",
				menubar: false,
				statusbar: false,

				gecko_spellcheck : true,

//				setup: function(editor) {
//			        editor.on('submit', function(e) {
//			            var nbChars = editor.getContent({format : 'raw'}).lenght;
//			            if(nbChars > 300){
//			            	confirm("Too big!");
//			            }
//			        });
//			    }


			});

	createTinyMceExtractBar("mceExtract,disableMCEInit");

}

window.addEventListener("load", createTinyMceExtract);