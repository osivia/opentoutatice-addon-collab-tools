var lang = 'fr';

function createTinyMce(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddEditor', false, textareas[index].id);
	}
}

window.onload = function(e) {

	tinyMCE
			.init({
				height: "150",
				mode : "specific_textareas",
				language : "fr",
				theme : "modern",
				editor_selector : "mceExtrait",
				editor_deselector : "disableMCEInit",
				plugins : "lists,directionality,nonbreaking,nuxeolink",

				// Theme options
				theme_advanced_buttons1 : "bold,italic,underline,|,strikethrough,sub,sup,|,justifyleft,justifycenter,justifyright,justifyfull,|,bullist,numlist,|,outdent,indent,blockquote,|,forecolor,backcolor,|,nuxeolink,link,unlink",
				menubar: false,

				gecko_spellcheck : true,

				relative_urls : false,
				remove_script_host : false
			});

	createTinyMce("mceExtrait,disableMCEInit");

}