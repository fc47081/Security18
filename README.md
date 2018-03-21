#Security17/18
#Grupo sc010

Security 17/18

Terminal commands:

server: javac PhotoShareServer.java (compilar)
        java PhotoShareServer
        
cliente: javac PhotoShare.java (compilar)
         java PhotoShare <localUserId> <password> <127.0.0.1:23232>


Para a realização de uma operação é necessário:
	1ºCorrer o PhotoShareServer como indicado acima;
	2ºCorrer o PhotoShare como indicado acima;
	3º Após a autenticação ou criação de user novo:
		3.1- Indicar a operação fazendo - (a,i,g,l,f,r,L,D,c);
		3.2- Seguido dos argumentos necessarios para cada operacao.
	
	Exemplo de cada comando :
		
		Sendo userId- um user válido e a fculLogo.jpg apenas um nome exemplo e adoro seguranca um 			comentario de exemplo também.

		-a: -a fculLogo.jpeg
		-i: -i userId fculLogo.jpg
		-g: -g userId
		-l: -l userId
		-f: -f userId 
		-r: -r userId 
		-L: -L userId fculLogo.jpg
		-D: -L userId fculLogo.jpg
		-c: -c adoro segurança userId fculLogo.jpg

