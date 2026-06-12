# Diseño: Solicitud de Vacaciones

## Objetivo

Crear un proyecto Maven llamado `vacaciones` que permita simular el registro de una solicitud de vacaciones mediante una interfaz JavaFX sencilla, ordenada y similar a la imagen de referencia.

## Alcance

La aplicación tendrá los siguientes campos:

- Nombre del empleado.
- Departamento.
- Cargo.
- Motivo de la solicitud.
- Fecha de inicio.
- Fecha de fin.

Todos los campos usarán `TextField`. La aplicación no validará, almacenará ni mostrará listados de información.

## Arquitectura

El proyecto seguirá el patrón utilizado en `holamundo`:

- `pom.xml`: configuración de Maven, Java 11, JavaFX 13 y `javafx-maven-plugin`.
- `App.java`: inicia JavaFX, carga el archivo FXML, aplica la hoja de estilos y configura la ventana.
- `VacacionesController.java`: administra las acciones Guardar y Limpiar.
- `vacaciones.fxml`: define la interfaz y sus layouts.
- `styles.css`: contiene todos los estilos visuales.

El paquete será `com.vacaciones`.

## Interfaz

La raíz será un `BorderPane`:

- Parte superior: cabecera azul con el texto “Solicitud de Vacaciones”.
- Centro: un `VBox` con el subtítulo “Datos del empleado”, tres filas `HBox` y dos campos por fila.
- Parte inferior del formulario: un `HBox` alineado a la derecha con los botones Guardar y Limpiar.
- Parte inferior de la ventana: zona clara con el texto “Estado: pendiente de revisión”.

La distribución de campos será:

1. Nombre del empleado y Departamento.
2. Cargo y Motivo de la solicitud.
3. Fecha de inicio y Fecha de fin.

La ventana tendrá un tamaño inicial aproximado de 1000 por 600 píxeles y podrá redimensionarse.

## Comportamiento

### Guardar

El botón Guardar mostrará un `Alert` de tipo información con:

- Título: `Solicitud de Vacaciones`
- Sin encabezado.
- Mensaje: `La solicitud de vacaciones ha sido registrada correctamente.`

No se limpiarán ni almacenarán los datos al guardar.

### Limpiar

El botón Limpiar dejará vacíos los seis `TextField`.

## Estilos

La hoja CSS definirá:

- Fuente legible para toda la aplicación.
- Cabecera azul oscuro con texto blanco.
- Fondo claro para el formulario.
- Etiquetas en azul oscuro y negrita.
- Campos con borde gris azulado.
- Botón Guardar azul con texto blanco.
- Botón Limpiar gris con texto blanco.
- Efectos `hover` sencillos para ambos botones.
- Espaciado y relleno suficientes para conservar una presentación ordenada.

## Verificación

El proyecto deberá:

- Compilar con `mvn clean compile`.
- Iniciar con `mvn clean javafx:run`.
- Mostrar los seis campos solicitados.
- Mostrar el mensaje de confirmación al presionar Guardar.
- Vaciar todos los campos al presionar Limpiar.
- Utilizar `BorderPane`, `VBox` y `HBox`.
- Cargar correctamente la hoja de estilos CSS.

