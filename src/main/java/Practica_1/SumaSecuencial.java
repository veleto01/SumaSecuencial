package Practica_1;

import java.util.Arrays;

import es.urjc.etsii.code.concurrency.SimpleSemaphore;

import static es.urjc.etsii.code.concurrency.SimpleConcurrent.*;

public class SumaSecuencial {

	public static final int N_PROCESOS = 5;
	public static final int N_DATOS = 16;

	private static volatile int[] datos = new int[N_DATOS];
	private static volatile int[] resultado = new int[N_DATOS];

	private static volatile int hilosTerminados = 0;
	private static volatile int NumeroSumados = 0;
	private static volatile int Nivel = 1;
	private static volatile int Incrementador = 0;
	private static volatile int GuardarResultado = 0;

	private static SimpleSemaphore emSumaPendiente;
	private static SimpleSemaphore emHilosTerminados;
	private static SimpleSemaphore UltimoHilo;
	private static SimpleSemaphore desbloqueo;

	private static void MostrarDatos() {
		
		if (datos.length == 1) {
			println("El resultado es : " + Arrays.toString(datos));
			println("--------------------------------------------------");
		} 
		else 
		{
			println("Los datos a sumar son: " + Arrays.toString(datos));
			println("--------------------------------------------------");
		}
		
	}

	private static int suma(int a, int b) {
		sleepRandom(100);
		return a + b;
	}

	private static void inicializaDatos() {
		for (int i = 0; i < N_DATOS; i++) {
			datos[i] = (int) (Math.random() * 11);
		}
		println("Los datos a sumar son: " + Arrays.toString(datos));
	}

	public static void concurrencia() {

		while (Nivel < 5) {
			
			SumadorParalelizado();
			
			emHilosTerminados.acquire();
			hilosTerminados++;
			if (hilosTerminados == N_PROCESOS) {
				if (datos.length != 2) {
					
					datos = Arrays.copyOf(resultado, (datos.length) / 2);
					println(getThreadName() + " Actualiza el array de datos a " + Arrays.toString(datos));
					
				}
				
				GuardarResultado = 0;
				println(getThreadName() + " Finalizado el nivel " + Nivel);
				
				if (Nivel == 4) {
					resultado[0] = datos[0] + datos[1];
					datos = Arrays.copyOf(resultado, (datos.length) / 2);
				}
				
				Nivel++;
			}

			if (hilosTerminados < N_PROCESOS) {
				emHilosTerminados.release();
				UltimoHilo.acquire();
				desbloqueo.release();
			} else {
				Incrementador = 0;
				GuardarResultado = 0;
				NumeroSumados = 0;
				MostrarDatos();
				hilosTerminados = 0;
				UltimoHilo.release(N_PROCESOS - 1);
				desbloqueo.acquire(N_PROCESOS - 1);
				emHilosTerminados.release();

			}
		}
	}

	private static void SumadorParalelizado() {
		while (true) {
			int sum = 0;
			
			emSumaPendiente.acquire();			
			if (Incrementador == datos.length) {
				println(getThreadName() + " Esperando a los demás procesos. Han terminado " + hilosTerminados
						+ " procesos.");
				emSumaPendiente.release();
				break;
			}
			
			int x = Incrementador;
			int y = Incrementador + 1;
			Incrementador = Incrementador + 2;
			int operando_1 = datos[x];
			int operando_2 = datos[y];
			NumeroSumados = NumeroSumados + 2;
			emSumaPendiente.release();

			
			println(getThreadName() + ":  Se inicia la suma de datos[" + x + "] datos[" + y + "]");
			sum = suma(operando_1, operando_2);
			emSumaPendiente.acquire();
			resultado[GuardarResultado] = sum;
			println(getThreadName() + ":  Se guarda la suma en resultados[" + GuardarResultado + "] = " + sum);
			GuardarResultado++;
			emSumaPendiente.release();
		}

	}

	public static void main(String[] args) {
		inicializaDatos();
		int sum = 0;
		for (int i = 0; i < N_DATOS; i++) {
			sum = suma(sum, datos[i]);
		}
		println("Suma: " + sum);

		
		
		emSumaPendiente   =	new SimpleSemaphore(1);
		emHilosTerminados = new SimpleSemaphore(1);
		UltimoHilo 		  = new SimpleSemaphore(0);
		desbloqueo 		  = new SimpleSemaphore(0);

		for (int i = 0; i < N_PROCESOS; i++) {
			createThread("concurrencia");
		}
		// createThreads(N_PROCESOS, "concurrencia");

		startThreadsAndWait();
	}
}