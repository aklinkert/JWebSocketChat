/**
 * @constructor
 * @public
 * @class Stellt die WebSocket-Verbindung sowie Methoden zum Umgang damit bereit.
 * @description Die Klasse SocketConnectionHandlerObject stellt Methoden bereit, um Daten via WebSocketConnection zu
 *              senden, empfangen, verarbeiten und kontrolliert zu verteilen. Objekte k&ouml;nnen sich bei dem
 *              SocketConnectionHandlerObject registrieren, um Updates zu WebTouchDataPaths zu bekommen.
 */
var SocketConnectionHandlerObject = function ( ) {
	/**
	 * @private
	 * @default String
	 * @description URL to connect to.
	 */
	this.url = null;
	
	/**
	 * @private
	 * @default WebSocket
	 * @description Instance of the WebSocket object.
	 */
	this.sockconn = null;
	
	/**
	 * @private
	 * @default Array
	 * @description Buffer for unsent messages.
	 */
	this.unsentMessages = new Array ( );
	
	this.messageHandler = null;
	
	// ################################################################
	// ----------Functions: OpenClose---------------------------------
	// ################################################################
	
	/**
	 * @function
	 * @public
	 * @param {String} url The URL to connect to.
	 * @description
	 */
	this.connect = function ( url ) {
		this.url = url;
		this.sockconn = null;
		
		this.sockconn = new WebSocket ( this.url );
		
		if ( ( typeof this.sockconn == "undefined" ) || ( this.sockconn == null ) )
			throw new Error ( "SocketConnectionHandler.connect: Socket Connection didn't establish" );
		
		this.sockconn.connectionHandler = this;
		
		this.sockconn.onopen = function ( evt ) {
			this.connectionHandler.sendBuffer ( );
			
			if ( this.connectionHandler.messageHandler.onOpen )
				this.connectionHandler.messageHandler.onOpen ( );
		};
		
		this.sockconn.onclose = function ( evt ) {
			if ( this.connectionHandler.messageHandler.onClose )
				this.connectionHandler.messageHandler.onClose ( );
		};
		
		this.sockconn.onmessage = function ( evt ) {
			this.connectionHandler.handleMessage ( evt.data );
		};
		
		this.sockconn.onerror = function ( evt ) {
			// Empty Function
		};
		
		this.sockconn.getTimeStamp = function ( ) {
			var d = new Date ( );
			return "<span style=\"color: green;\">" + d.getHours ( ) + ":" + d.getMinutes ( ) + ":" + ( ( d.getSeconds ( ) < 10 ) ? "0" + d.getSeconds ( ).toString ( ) : d.getSeconds ( ).toString ( ) ) + ":" + d.getMilliseconds ( ) + "</span>";
		};
		
		this.sockconn.getReadyState = function ( ) {
			return this.readyState;
		};
		
	};
	
	/**
	 * @function
	 * @public
	 * @description Closes the WebSocket-Connection.
	 */
	this.close = function ( ) {
		this.sockconn.close ( );
	};
	
	// ################################################################
	// ----------Functions: Send---------------------------------------
	// ################################################################
	
	/**
	 * @function
	 * @public
	 * @param {String} message The message which should be sent.
	 * @description Sends a message over the WebSocket-Connection.
	 */
	this.send = function ( message ) {
		if ( this.sockconn == undefined || this.sockconn == null )
			this.connect ( this.url );
		
		if ( this.sockconn.getReadyState ( ) == 1 )
			this.sockconn.send ( message );
		else
			this.unsentMessages.push ( message );
	};
	
	/**
	 * @function
	 * @private
	 * @description Sends the message which are currently in the buffer.
	 */
	this.sendBuffer = function ( ) {
		while ( this.unsentMessages.length > 0 ) {
			if ( this.sockconn.getReadyState ( ) == 1 )
				this.send ( this.unsentMessages.shift ( ) );
			else
				return;
		}
		
	};
	
	// ################################################################
	// ----------Functions: handle messages----------------------------
	// ################################################################
	
	/**
	 * @function
	 * @public
	 * @param {MessageHandlerObject} obj The {@link MessageHandlerObject}, which should handle the incoming messages.
	 * @description Defines a MessageHandler for the incoming messages.
	 */
	this.setMessageHandler = function ( obj ) {
		this.messageHandler = obj;
	};
	
	/**
	 * @function
	 * @private
	 * @param {String} msg The received message.
	 * @description Handles the incoming message.
	 */
	this.handleMessage = function ( msg ) {
		this.messageHandler.handle ( msg );
		
	};
};
