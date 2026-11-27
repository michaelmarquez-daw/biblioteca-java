# Script auxiliar solo para hacer docs/captura.png, no forma parte del programa.
# Lanza el jar, le va escribiendo las respuestas cuando aparece cada pregunta
# y guarda la sesión real (con lo tecleado) en sesion.txt y captura.html.
# Trabaja sobre una copia de datos/ en una carpeta temporal, así los datos de
# ejemplo no se tocan aunque el script falle a medias.
# Uso (desde la carpeta del proyecto, después de mvn package):
#   python docs/generar_captura.py
#   chrome --headless --screenshot=docs/captura.png --window-size=1280,800 docs/captura.html
import html
import queue
import shutil
import subprocess
import tempfile
import threading
import time
from pathlib import Path

PROYECTO = Path(__file__).resolve().parent.parent
DOCS = PROYECTO / "docs"

RESPUESTAS = [
    "3", "4",
    "1", "9788408041597", "3",
    "1", "9788408041597", "1",
    "1", "9788408041597", "5",
    "2", "9788466307666",
    "x", "0", "0",
]


def leer_salida(proceso, cola):
    while True:
        caracter = proceso.stdout.read(1)
        if not caracter:
            break
        cola.put(caracter)


def esperar_texto(cola, pausa=0.4):
    texto = ""
    ultimo = time.time()
    while time.time() - ultimo < pausa:
        try:
            texto += cola.get(timeout=0.05)
            ultimo = time.time()
        except queue.Empty:
            pass
    return texto


def grabar_sesion():
    tmp = Path(tempfile.mkdtemp())
    shutil.copytree(PROYECTO / "datos", tmp / "datos")
    try:
        proceso = subprocess.Popen(
            ["java", "-Dstdout.encoding=UTF-8", "-jar", str(PROYECTO / "target" / "biblioteca.jar")],
            cwd=tmp, stdin=subprocess.PIPE, stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT, text=True, encoding="utf-8")
        cola = queue.Queue()
        threading.Thread(target=leer_salida, args=(proceso, cola), daemon=True).start()

        sesion = "C:\\biblioteca> java -jar target\\biblioteca.jar\n"
        for respuesta in RESPUESTAS:
            sesion += esperar_texto(cola)
            sesion += respuesta + "\n"
            proceso.stdin.write(respuesta + "\n")
            proceso.stdin.flush()
        proceso.wait(timeout=10)
        sesion += esperar_texto(cola)
    finally:
        shutil.rmtree(tmp)
    # La ruta temporal es larga y personal; en la captura se ve como C:\biblioteca
    return sesion.replace(str(tmp), "C:\\biblioteca")


def crear_html(sesion):
    lineas = sesion.rstrip("\n").split("\n")
    mitad = (len(lineas) + 1) // 2
    # Se corta por una línea en blanco cercana para no partir un menú por la mitad
    for i in range(mitad, len(lineas)):
        if lineas[i] == "":
            mitad = i
            break
    columnas = ["\n".join(lineas[:mitad]), "\n".join(lineas[mitad + 1:])]
    bloques = "".join(f"<pre>{html.escape(c)}</pre>" for c in columnas)
    plantilla = (DOCS / "plantilla_captura.html").read_text(encoding="utf-8")
    return plantilla.replace("{{SESION}}", bloques)


if __name__ == "__main__":
    sesion = grabar_sesion()
    (DOCS / "sesion.txt").write_text(sesion, encoding="utf-8")
    (DOCS / "captura.html").write_text(crear_html(sesion), encoding="utf-8")
    print(sesion)
