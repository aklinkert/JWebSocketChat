/**
 * @constructor
 * @public
 * @class The global Object for object initializing and handling.
 * @description This object handles the needed objects like the {@link SocketConnectionHandlerObject} or the
 *              {@link MessageHandlerObject}. It is responsible for its initialization and its handling.
 */

var JSWSMCCObject = function ( ) {
	/**
	 * @public
	 * @default null
	 * @description Instance of the {@link SocketConnectionHandlerObject}.
	 */
	this.socketConnectionHandler = null;
	
	/**
	 * @public
	 * @default null
	 * @description Instance of the {@link MessageHandlerObject}.
	 */
	this.messageHandler = null;
	
	/**
	 * @function
	 * @public
	 * @description Initializes all required objects.
	 */
	this.init = function ( ) {
		this.socketConnectionHandler = new SocketConnectionHandlerObject ( );
		
		// The constructor needs a Settings-Object being given
		this.messageHandler = new MessageHandlerObject ( {
		connectionHandler: this.socketConnectionHandler ,
		container: $ ( "#messageOutput" ) ,
		inputField: $ ( "#messageInput" ) ,
		inputSubmitButton: $ ( "#messageInputSubmit" ) ,
		userList: $ ( "#userList" ) ,
		toEnable: $ ( ".toEnable" ) ,
		toDisable: $ ( ".toDisable" ) ,
		nameInput: $ ( "#nameInput" ) ,
		urlInput: $ ( "#urlInput" ) ,
		connectButton: $ ( "#btnConnect" ) ,
		disconnectButton: $ ( "#btnDisconnect" ) ,
		noUserID: "nouser",
		nouserEntry: "No user in Chat."
		} );
		
		// Initialize default stuff
		this.messageHandler.init ( );
		
		// Connect the SocketConnectionEventHandler and the MessageHandler
		this.socketConnectionHandler.setMessageHandler ( this.messageHandler );
		
	};
};
