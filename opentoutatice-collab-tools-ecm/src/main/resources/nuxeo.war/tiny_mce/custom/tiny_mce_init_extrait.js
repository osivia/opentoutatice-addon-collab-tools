var lang = 'fr';

function createTinyMce(clazz) {
	var textareas = document.getElementsByClassName(clazz);
	for ( var index = 0; index < textareas.length; index++) {
		tinyMCE.execCommand('mceAddControl', false, textareas[index].id);
	}
}

window.onload = function(e) {

	tinyMCE
			.init({
				height: "150",
				mode : "specific_textareas",
				language : "fr",
				theme : "advanced",
				editor_selector : "mceExtrait",
				editor_deselector : "disableMCEInit",
				plugins : "lists,style,advlink,directionality,nonbreaking,xhtmlxtras,nuxeolink",

				// Theme options
				theme_advanced_buttons1 : "bold,italic,underline,strikethrough,sub,sup|,justifyleft,justifycenter,justifyright,justifyfull,|,bullist,numlist,|,outdent,indent,blockquote,|,forecolor,backcolor,|,nuxeolink,link,unlink",

				gecko_spellcheck : true,

				// Skin options
				skin : "o2k7",
				skin_variant : "silver",
				relative_urls : false,
				remove_script_host : false
			});

	createTinyMce("mceExtrait,disableMCEInit");

}