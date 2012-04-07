var MessageHandlerObject = function ( settings ) {
	/**
	 * @private
	 * @default Object
	 * @description A given Object with the necessary Fields.
	 */
	this.settings = settings;
	
	/**
	 * @private
	 * @default SocketConnectionHandlerObject
	 * @description The SocketConnection used for this Chat.
	 */
	this.connectionHandler = new SocketConnectionHandlerObject();
	
	/**
	 * @private
	 * @default RegEx
	 * @description A regular expression to match an incoming command.
	 * 
	 */
	this.commandpattern = /([a-zA-Z]{4}) (.+)/;
	
	/**
	 * @private
	 * @default RegEx
	 * @description Regular Expression to match an incomin message. Needed to output it in a desktop notification.
	 */
	this.messagepattern = /([0-9:]+)\s(.+)/;
	
	/**
	 * @private
	 * @default String
	 * @default Stores the name of the user.
	 */
	this.userName = "";
	
	/**
	 * @private
	 * @default Boolean
	 * @description True, if a name change request was already sent.
	 */
	this.sendNameChangeRequest = false;
	
	/**
	 * @private
	 * @default null
	 * @description Empty variable for the popup.
	 */
	this.popup = null;
	
	/**
	 * @public
	 * @function
	 * @description Initializes some useful stuff like desktop notifications.
	 */
	this.init = function ( ) {
		that = this;
		
		this.connectionHandler.setMessageHandler ( this );
		
		if ( ! window.webkitNotifications )
			this.outputMessage ( "Sorry , your browser does not support desktop notifications. Try Google Chrome." );
		else
			$ ( document ).one ( "click" , function ( ) {
				that.requestDesktopNotificationPermission ( );
			} );
		
		this.settings.inputField.on ( "keyup" , function ( event ) {
			if ( ( $ ( this ).val ( ) != "" ) && ( $ ( this ).val ( ) != " " ) && ( event.keyCode == 13 ) )
				that.settings.inputSubmitButton.trigger ( "click" );
		} );
		
		// Add Events to the Inputs
		this.settings.inputSubmitButton.on ( "click" , function ( ) {
			that.sendInput ( that.settings.inputField.val ( ) );
			that.settings.inputField.val ( "" ).focus ( );
		} );
		
		this.settings.connectButton.on ( "click" , function ( ) {
			try {
				that.connectionHandler.connect ( that.settings.urlInput.val ( ) );
			}
			catch ( e ) {
				// logToConsole ( e.toString ( ) );
			}
		} );
		this.settings.disconnectButton.on ( "click" , function ( ) {
			that.connectionHandler.close ( );
		} );
	};
	
	/**
	 * @public
	 * @function
	 * @param {String} msg The message which should be handled.
	 * @description Handler-Method for incoming messages.
	 */
	this.handle = function ( msg ) {
		var parts = msg.match ( this.commandpattern );
		
		if ( parts.length != 3 )
			throw new ErrorObject ( "MessageHandlerObject", "handle", "The incoming message could not be parsed." );
		
		try {
			switch ( parts [ 1 ] ) {
				case "umsg" :

					this.outputMessage ( parts [ 2 ] );
					
					var notText = parts [ 2 ].match ( this.messagepattern );
					this.showDesktopNotification ( notText [ 2 ] );
					
					break;
				case "left" :

					this.settings.userList.children ( ).remove ( "#" + "user_" + parts [ 2 ] );
					
					if ( this.settings.userList.children ( ).size ( ) == 0 )
						this.appendUserListEntry ( this.createUserEntry ( this.settings.noUserID ).text ( this.settings.nouserEntry ) );
					
					this.outputMessage ( this.getTimeStamp ( ) + " " + parts [ 2 ] + " left." );
					
					break;
				case "join" :

					if ( parts [ 2 ] == this.userName || this.userName == "" ) {
						this.userName = parts [ 2 ];
						this.sendNameChangeRequest = false;
						this.settings.toEnable.removeAttr ( "disabled" );
						this.settings.nameInput.removeAttr ( "disabled" );
						this.settings.inputField.removeAttr ( "disabled" );
						this.getUserList ( );
						return;
					}
					
					this.settings.userList.children ( ).filter ( "#user_" + this.settings.noUserID ).remove ( );
					
					this.appendUserListEntry ( this.createUserEntry ( parts [ 2 ] ) );
					this.outputMessage ( this.getTimeStamp ( ) + " " + parts [ 2 ] + " joined." );
					
					break;
				case "user" :

					if ( parseInt ( parts [ 2 ] ) == 0 || ( parts [ 2 ] == this.userName ) ) {
						this.appendUserListEntry ( this.createUserEntry ( this.settings.noUserID ).text ( this.settings.nouserEntry ) );
						return;
					}
					
					var userListStr = parts [ 2 ].split ( "§§" );
					
					for ( user in userListStr )
						if ( userListStr [ user ] != this.userName )
							this.appendUserListEntry ( this.createUserEntry ( userListStr [ user ] ) );
					
					break;
				case "fail" :

					switch ( parseInt ( parts [ 2 ] ) ) {
						case 1 :

							this.outputMessage ( "The username you chose is already in user list!" );
							
							this.sendNameChangeRequest = false;
							if ( this.userName != "" )
								this.settings.nameInput.val ( this.userName );
							
							this.settings.nameInput.removeAttr ( "disabled" );
							
							break;
						default :
							alert ( "Failed: " + parts [ 2 ] );
							break;
					}
					
					break;
				case "inka" :

					// Command: inka name_alt:name_neu
					var names = parts [ 2 ].split ( ":" );
					
					this.outputMessage ( this.getTimeStamp ( ) + " " + names [ 0 ] + " is now known as " + names [ 1 ] + "." );
					
					if ( names [ 0 ] == this.userName ) {
						this.userName = names [ 1 ];
						this.sendNameChangeRequest = false;
						this.settings.nameInput.removeAttr ( "disabled" );
						return;
					}
					
					this.settings.userList.children ( ).remove ( "#user_" + names [ 0 ] );
					this.appendUserListEntry ( this.createUserEntry ( names [ 1 ] ) );
					
					break;
				default :

					break;
			}
		}
		catch ( e ) {
			logToConsole ( e.toString ( ) );
		}
	};
	
	/**
	 * @public
	 * @function
	 * @description Handler-Method for onOpen-Event.
	 */
	this.onOpen = function ( ) {
		this.settings.toEnable.removeAttr ( "disabled" );
		this.settings.toDisable.attr ( "disabled" , true );
		this.settings.inputField.attr ( "disabled" , true );
		
		this.sendNameChangeRequest = false;
		this.settings.nameInput.attr ( "disabled" , true ).on ( "blur" , function ( ) {
			that.setName ( );
		} ).trigger ( "blur" );
		
		this.outputMessage ( this.getTimeStamp ( ) + " Connected." );
	};
	
	/**
	 * @public
	 * @function
	 * @description Handler-Method for onOpen-Event.
	 */
	this.onClose = function ( ) {
		this.outputMessage ( this.getTimeStamp ( ) + " Connection lost. Click on \"Connect\" to reestablish connection." );
		
		this.userName = "";
		this.settings.userList.empty ( );
		this.settings.toDisable.removeAttr ( "disabled" );
		this.settings.toEnable.attr ( "disabled" , true );
		this.settings.nameInput.off ( "blur" );
	};
	
	/**
	 * @private
	 * @function
	 * @param {String} name The name (used as ID and text) of the user, which should be appended at the userList.
	 * @description Creates a new Entry for the UserList.
	 */
	this.createUserEntry = function ( name ) {
		return $ ( "<div>" ).attr ( "id" , "user_" + name ).text ( name );
	};
	
	/**
	 * @private
	 * @function
	 * @param {jQueryObject} entry The entry which should be appended.
	 * @description Appends an entry at the userList container.
	 */
	this.appendUserListEntry = function ( entry ) {
		this.settings.userList.append ( entry );
	};
	
	/**
	 * @private
	 * @function
	 * @param {String} message The message which should be appended.
	 * @description Appends a message at the output container.
	 */
	this.outputMessage = function ( message ) {
		this.settings.container.append ( $ ( "<br />" ) ).append ( message ).scrollTop ( this.settings.container.prop ( "scrollHeight" ) );
	};
	
	/**
	 * @public
	 * @function
	 * @param {String} msg The message which should be send.
	 * @description Handler-Method for outgoing messages.
	 */
	this.sendInput = function ( msg ) {
		this.connectionHandler.send ( "umsg " + msg );
	};
	
	/**
	 * @public
	 * @function
	 * @param {String} name The Name to be set.
	 * @description Sets the Name of the user.
	 */
	this.setName = function ( ) {
		
		if ( ( this.settings.nameInput.val ( ) != this.userName ) && ( ! this.sendNameChangeRequest ) ) {
			this.connectionHandler.send ( "name " + this.settings.nameInput.attr ( "disabled" , true ).val ( ) );
			this.sendNameChangeRequest = true;
		}
	};
	
	/**
	 * @public
	 * @function
	 * @description Sends a command message to get the userList with all users, that are logged in at the moment.
	 */
	this.getUserList = function ( ) {
		this.connectionHandler.send ( "user all" );
	};
	
	/**
	 * @public
	 * @function
	 * @returns String
	 * @description Returns a timestamp formatted for the messageOutput.
	 */
	this.getTimeStamp = function ( ) {
		var d = new Date ( );
		return "[" + d.getHours ( ) + ":" + d.getMinutes ( ) + ":" + ( ( d.getSeconds ( ) < 10 ) ? "0" + d.getSeconds ( ).toString ( ) : d.getSeconds ( ).toString ( ) ) + "]:";
	};
	
	/**
	 * @public
	 * @function
	 * @description Requests for the permission to show Desktop Notifications.
	 */
	this.requestDesktopNotificationPermission = function ( ) {
		if ( window.webkitNotifications.checkPermission ( ) != 0 ) // 0 is PERMISSION_ALLOWED
			window.webkitNotifications.requestPermission ( function ( ) {
				if ( window.webkitNotifications.checkPermission ( ) == 0 )
					that.showDesktopNotification = that.showDesktopNotificationFunction;
			} );
		else
			that.showDesktopNotification = that.showDesktopNotificationFunction;
	};
	
	/**
	 * @private
	 * @function
	 * @param {String} text The text to be shown.
	 * @description Blind Function! Function will be replaced if the user grants permission for Desktop Notifications.
	 */
	this.showDesktopNotification = function ( text ) {
		// Empty function
	};
	
	/**
	 * @private
	 * @function
	 * @param {String} text The text to be shown.
	 * @description Shows a desktop notification.
	 */
	this.showDesktopNotificationFunction = function ( text ) {
		if ( typeof this.popup != "undefined" && this.popup != null )
			return;
		
		if ( ! document.hasFocus ( ) ) {
			this.popup = window.webkitNotifications.createNotification ( "http://www.iconeasy.com/icon/thumbnails/System/WebGloss%203D/Message%20Icon.jpg" , "New Message" , text );
			this.popup.show ( );
			setTimeout ( function ( ) {
				that.popup.cancel ( );
				that.popup = null;
			} , 3000 );
		}
	};
};
