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

				// Theme options
				toolbar1 : "bold italic underline | strikethrough subscript | alignleft aligncenter alignright alignjustify | bullist numlist | outdent indent blockquote | forecolor backcolor | nuxeolink link unlink",
				menubar: false,
				statusbar: false,

				gecko_spellcheck : true,

				relative_urls : false,
				remove_script_host : false
			});

	createTinyMceExtractBar("mceExtract,disableMCEInit");

}

window.addEventListener("load", createTinyMceExtract);