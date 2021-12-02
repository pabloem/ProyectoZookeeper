# ProyectoZookeeper
Primero crea un loker de nodos persistente, cada vez que se adquiera un bloqueo, se crea un nodo de secuencia temporal 
debajo del loker y elimina el nodo después de que se agota el bloqueo.
Cada vez que crea un nodo, el programa debe checar si el nodo es el más pequeño, por lo que primero debe ordenar los nodos
y obtener el nodo que es más grande que el que esta.
Después de obtener un nodo más grande, se activa el reloj que monitorea la salida. 
Nota: Un nodo solo monitoreará el nodo más grande que él, evitando el efecto manada.
