JWebSocketChat
==============

JWebSocketChat is a WebChat made up of a Java server and a JavaScript client. The communication between server and client is done using WebSockets ([Java-WebSocket project](https://github.com/TooTallNate/Java-WebSocket "GitHub repository")).


Example
-------------------

You can try JWebSocketChat at [http://jswsmcc.arda-network.de](http://jswsmcc.arda-network.de "http://jswsmcc.arda-network.de")


Starting the Server
-------------------

To start the server you have to pass a host and a port. To bind the server to localhost:444 you have to run the following:

```bash
java -jar JWebSocketServer.jar localhost 444
```


Using the Client
-------------------

The index.html located in /Example/Client is everything you need. You can use it by simply opening it in a Browser. To connect to the server just type the address of the server in WebSocket-Format in the input field below the namelist. To connect to the server configured in this example insert ws://localhost:444, enter a name and click on connect.


Design your own Client
----------------------

At first you need your own Design which should contain the following Elements (Elements in braces are optional):

* Container for the message output
* Container for the user list
* Input field for the message to send
* (Input field for Server URI)
* (send button)
* (Connect Button)
* (Disconnect Button)

Now you have to create an instance of the MessageHandlerObject and pass your Elements in a object containing jQuery objects. If the Input field for the message or other objects should be enabled or disabled if connection is lost or not established you've got the possibillity to pass it either. you **MUST** pass all the options. If you don't wan't the functionality you have to pass an empty jQuery container (e.g. $("&lt;div&gt;")).

The initialisation of the MessageHandlerObject could be look like this:

```JavaScript
messageHandler = new MessageHandlerObject ( {
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

messageHandler.init ( );
```

What are this parameters for?

* *container*: The container in which the messages and outputs should be placed in.
* *inputField*: The input field the user types new messages in.
* *inputSubmitButton*: The send-button. If you don't want a "send"-Button in your Design you are able to pass a object like this: $("&lt;input type="text" /&gt;").
* *userList*: The container in which the connected users will be shown in.
* *toEnable*: A set containing elements to enable when the connection is established. This paramter is optional and can be given with f.e. an empty div ($("&lt;div&gt;").
* *toDisable*: A set containing elements to disable when the connection is established. This paramter is optional and can be given with f.e. an empty div ($("&lt;div&gt;").
* *nameInput*: The input field where the user has to enter his name in.
* *urlInput*: An input field for the server URI. If you want to force the usage of a server then you could pass an generated input field with the server URI instead (like this: $("&lt;input type="text" /&gt;")
* *connectButton*: The button used to connect to the server. Could be triggered by script.
* *disconnectButton*: The button used to disconnect from server.
* *noUserID*: The id for the "No users in Chat." entry.
* *nouserEntry*: The "No users in Chat." label (text within the div with the id given with *noUserID*).


Browser Compatibility
---------------------

If you care for the browsers which don't have native WebSocket support yet you can additionally include the files provided by [web-socket-js](https://github.com/gimite/web-socket-js "web-socket-js"). Those scripts will try to establish WebSocket connections using Flash when necessary.

Please consider that some browsers only allow Flash socket connections to port 843 because of Flash policy restrictions.