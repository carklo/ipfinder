# Ipfinder
Desarrollado por Ing. José Santiago Ardila Acuña. LinkedIn: https://www.linkedin.com/in/santiago-ardila-acuña-8991a63a/

## Aspectos generales e infraestructura
El proyecto fue desarrollado bajo el framework web de Spring usando el JDK 11, se construyó una estructura convencional de capas Controller, Service, Repository 
para crear una API Rest que consulta la información asociada a una IP.

De manera general cuando entra una petición para consultar la información geográfica y monetaria de una IP se hace la consulta a los servicios externos propuestos
en el enunciado de la prueba y se almacena en base de datos esta información, se hace uso tambien de una estructura de Cache en memoria para que si se detecta que
ya se ha consultado antes la información de un país o su moneda se consulte sobre la memoria cache y no se hagan llamadas a los servicios externos que en tiempo
de ejecución son los mas costosos.


### Infraestructura cloud
[![fsj4Du.md.png](https://iili.io/fsj4Du.md.png)](https://freeimage.host/i/fsj4Du)

Para atender al requerimiento sobre el primer endpoint el cual va a recibir fluctuaciones agresivas de tráfico se diseño una infraestructura cloud la cual esta implementada
sobre AWS (Amazon Web Services). En el anterior diagrama se muestran los principales componentes implementados con el fin de dar soporte al requerimiento de carga,
los principales se mencionan a continuación:

* AWS RDS MySQL: Servicio de base datos con el motor MySQL el cual aloja la información geográfica de todas las IP consultadas, la información asociada a código ISO y
la moneda con su respectiva cotización, se usó con el fin de mantener un registro permanente y un estado duradero en el tiempo de las IP consultadas.

* AWS ECR: Servicio que dispone de un repositorio en el cual se almacenan contenedores Docker que posteriormente se vincula para poder ejecutar la imágen del proyecto
sobre Tasks.

* AWS ECS Tasks: Servicio que permite definir tasks o tareas las cuales se pueden correr en modo FARGATE (como un contenedor Docker) o EC2 (usar una instancia de
procesamiento, lo mas común una maquina con un sistema operativo).

* AWS ECS Cluster: Servicio que sirve para formar un cluster o pool de instancias ECS, en este proyecto se agrupan bajo un service que tiene la definición de cuantas
tasks o tareas se van a ejecutar de manera paralela.

* AWS ECS Service: Servicio que corre dentro de un cluster el cual agrupa varias tasks en forma de tasks groups, se configura aquí parametros para el escalado automático
asi como la conexión con el balanceador de carga.

* AWS ELB: Balanceador de carga elástico, en este caso se usa como entrada principal de la API Rest el cual se encarga de distribuir las peticiones de manera equitativa,
garantizando asi una distribución inteligénte de la carga.

De esta manera la infraestructura cloud puede atender multiples peticiones concurrentes, así mismo en caso de que las tasks que estan respondiendo a las peticiones
queden inhabilitadas por un pico muy alto de tráfico el estado de la aplicación se estará almacenando en una base de datos relacional y la infraestructura se encargará
de recuperarse automáticamente en el menor tiempo posíble para poder seguir atentiendo solicitudes.


## Endpoints
| Método | Path                      | Descripción     | Respuesta Ejemplo |
|:-------|:------------------------- |:----------------|:----------------------|
| `GET ` | `http://challenge-lb-254874731.us-east-1.elb.amazonaws.com/ipfinder/ip/{ip}` | Dada una IP que entra por parametro dentro de la URL se consulta la información geográfica (país, ISO code) así como su cotización en USD, en caso de que la IP se haya baneado se mostrara un mensaje de error en vez de la información respectiva.| `{"ip":"129.204.20.248","country":"China","isoCode":{"alphaCode2":"CN","alphaCode3":"CHN","numericCode":"156"},"localCurrency":{"code":"CNY","name":"Chinese yuan","symbol":"¥","quotation":"6.465801 USD"},"banned":false} ` | 
| `POST ` | `http://challenge-lb-254874731.us-east-1.elb.amazonaws.com/ipfinder/banIp/{ip}` |Dada una IP que entra por parametro de la URL se consulta si esa IP ya existe dentro de la base de datos, en caso de que no se crea una IP con información vacía y un atributo que la identifica como no consultable, en caso de que existe (es decir, que fue consultada en el punto 1) se marca para no ser mas consultada.| `The IP 85.214.132.117 was banned ` | 


## Ejecución como contenedor Docker local
Bajo la raíz principal del proyecto se encuentra el archivo Dockerfile el cual permite ejecutar el proyecto como un contenedor Docker.
Para ejecutar el proyecto de manera local usar los siguientes comandos dentro de la raiz del proyecto:
`docker build -t ipfinder/challenge .`
`docker run -p 8080:8080 ipfinder/challenge`

## Pruebas unitarias y funcionales
Las pruebas unitarias se ubican bajo el directorio `src/test/java/com/meli/challenge/`, en resumen se realizaron un total de 17 test unitarios para la clase Controller
y la clase Service que contienen la logica principal.

```
[INFO] Results:
[INFO] 
[INFO] Tests run: 17, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] 
[INFO] --- maven-jar-plugin:3.2.0:jar (default-jar) @ challenge ---
[INFO] 
[INFO] --- spring-boot-maven-plugin:2.4.2:repackage (repackage) @ challenge ---
[INFO] Replacing main artifact with repackaged archive
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  18.676 s
[INFO] Finished at: 2021-02-26T15:57:07-05:00
[INFO] ------------------------------------------------------------------------

```
