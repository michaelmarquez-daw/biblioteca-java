# Gestor de biblioteca

Aplicación de consola en Java para una biblioteca pequeña: libros, socios y préstamos. Un socio puede tener como mucho 3 libros a la vez, no se puede prestar un libro que ya está fuera y el programa avisa de los préstamos que pasan de 14 días. Los datos se guardan en CSV en la carpeta `datos/`.

La hice para Programación y Entornos de desarrollo (1º de DAW): clases que validan sus datos, un `enum`, excepciones propias, colecciones y streams, fechas con `LocalDate`, ficheros con `java.nio`, Maven y tests con JUnit 5.

![Préstamos y devoluciones en la consola](docs/captura.png)

## Cómo se ejecuta

Con el JDK 21 y Maven, desde la carpeta del proyecto:

    mvn package
    java -jar target/biblioteca.jar

`mvn package` pasa también los tests. Los cambios se guardan al salir con la opción 0. Si las tildes salen mal en la consola de Windows, ejecuta antes `chcp 65001`.

## Cosas que decidí

El código está separado en `modelo/` (Libro, Socio, Prestamo, Genero), `servicio/` (la clase Biblioteca con las reglas y las excepciones), `persistencia/` (los CSV) y `ui/` (el menú).

Las reglas están en `Biblioteca` y no en el menú para poder probarlas con JUnit sin simular el teclado. Por eso los métodos que dependen del día reciben la fecha como parámetro en vez de usar `LocalDate.now()`: si no, para probar un retraso de 5 días tendría que esperar 5 días.

Los CSV van separados por `;` porque en los títulos hay comas a menudo, y el programa no deja meter `;` en los textos. Si un fichero tiene una línea mal o un ISBN repetido, el programa no arranca y dice qué pasa, para no sobrescribir los datos buenos al salir.

Lo que no está: solo se guarda al salir, y los títulos que empiezan con tilde se ordenan mal ("Ébano" sale después de la Z).
