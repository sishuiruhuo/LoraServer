package main;

import socket.Server;

public class ServerMain {

	public static void main(String[] args)
	{
		
		Server server = new Server();
		
		new Thread(server,"nio-server").start();
		
	}
}
