$ ( document ).ready ( function ( ) {
	
	$ ( ".content" ).css ( {
	"height": ( window.innerHeight * 0.9 ) ,
	"width": ( window.innerWidth * 0.9 ) ,
	"margin-top": ( window.innerHeight * 0.05 ) ,
	"margin-left": ( window.innerWidth * 0.05 )
	} );
	
	jswsmcc = new JSWSMCCObject ( );
	jswsmcc.init ( );
	
} );

function logToConsole ( msg ) {
	console.debug ( msg );
	$ ( "#logOutput" ).append ( $ ( "<div>" ).html ( msg ) );
}
