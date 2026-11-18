# minidropbox-distribuidos - Ana Nicolás y Julia Saenz

SISTEMA DE SINCRONIZACIÓN DE FICHEROS EN RED

## Descripción
Sistema cliente-servidor en Java que permite tener sincronizada una carpeta local entre varios clientes y un servidor central. Hacemos uso de sockets TCP, flujo de datos binarios, concurrencia con pool de hilos...

## Estructura del proyecto 
- 'compartido': clases comunes, definición del protocolo de red, transferencia de bloques.
-'servidor': Servidor concurrente, gestión del almacenamiento e índice seguro en memoria 
* 'cliente': Aplicación de consola para interactuar con el servidor y sincronizar ficheros.


## Protoclo de comunicación (TCP)
'HELLO <usuario>' -> 'OK'
'LIST' -> 'OK' + mostrar lista de ficheros que hay en el servidor
'PUT <nombre> <fecha>' -> 'OK' + enviar bytes -> 'OK'
'GET' <nombre> -> 'OK' + recepción de bytes
'BYE' -> cierre de conexión