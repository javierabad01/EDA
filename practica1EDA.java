import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.text.DecimalFormat;

/**
 * Clase que contiene los dos usuarios de cada unión 
 * @author Javier Abad Hernández
 *
 */
class listaUsuarios{
	private int usr1;
	private int usr2;
	
	
	public listaUsuarios(){
        usr1 = 0;
        usr2 = 0;
    }
    public listaUsuarios(int usr1, int usr2){
        this.usr1 = usr1;
        this.usr2 = usr2;
    }
    public int getUsr1(){
        return this.usr1;
    }
    public int getUsr2(){
        return this.usr2;
    }
	public void setUsr1(int usr1) {
		this.usr1=usr1;
	}
	public void setUsr2(int usr2) {
		this.usr2=usr2;
	}
}

/**
 * Clase principal en la que se realizan todas las operaciones 
 * @author Javier Abad Hernández
 *
 */
public class practica1EDA {

	public static void main(String[] args) throws IOException, FileNotFoundException {
		ArrayList<listaUsuarios> red = new ArrayList<>();
		ArrayList<Integer> usr = new ArrayList<>();
		ArrayList<ArrayList<Integer>> grus = new ArrayList<>();
		ArrayList<Integer> asig = new ArrayList<>();
		DecimalFormat formato = new DecimalFormat("00.00");

		int numUser = 0;
		int numConex = 0;
		Scanner in = new Scanner (System.in);

		System.out.println("ANALISIS DE CARALIBRO");
		System.out.println("---------------------");
		System.out.print("Fichero principal: ");
				
		String nombreFichero = in.nextLine();		
		Scanner redes=new Scanner (new File(nombreFichero));
		
		double tiempoIniRed = System.currentTimeMillis();

		if (redes.hasNextInt()) {
			numUser=redes.nextInt();
			numConex=redes.nextInt();
			
			crearRed(redes, red);
		}
		double tiempoLectura = System.currentTimeMillis();
		
		System.out.println("Lectura fichero " + ((tiempoLectura-tiempoIniRed)*0.001 + " seg."));
		
		System.out.print("Fichero de nuevas conexiones (pulse enter si no existe): ");

		String nombreFicheroAdicional = in.nextLine();

		redes=new Scanner (new File("extra.txt"));
		int conexExtra=nuevasConexiones(redes,nombreFicheroAdicional);
		if (conexExtra!=0) {
			numConex+=conexExtra;
			redes=new Scanner (new File("extra.txt"));
			listaExtra(redes, red, nombreFicheroAdicional, numConex);
		}
		redes.close();
		
		System.out.println(numUser + " usuarios "+ numConex + " conexiones");
		System.out.print("Porcentaje tamaño mayor grumo: ");

		double porcentaje = in.nextDouble();
				
		PrintWriter salida = new PrintWriter ("extra.txt");
		
		double tiempoIniUsr = System.currentTimeMillis();

		listaUsr(usr, red, numConex);

		double tiempoFinUsr = System.currentTimeMillis();

		System.out.println("Creación lista usuarios: " + ((tiempoFinUsr-tiempoIniUsr)*0.001 + " seg."));

		double tiempoIniGrus = System.currentTimeMillis();
		
		listaGrus(usr, red, asig, grus);		
		
		double tiempoFinGrus = System.currentTimeMillis();
		System.out.println("Creación lista grumos: " + ((tiempoFinGrus-tiempoIniGrus)*0.001 + " seg."));
		int grumosIni = grus.size();

		double tiempoIniOrdGrus = System.currentTimeMillis();

		// Ordenar Arraylist segun su size
		grus.sort((x,y) -> y.size() - x.size());
		
		double tiempoFinOrdGrus = System.currentTimeMillis();
		System.out.println("Ordenación y selección de grumos: " + ((tiempoFinOrdGrus-tiempoIniOrdGrus)*0.001 + " seg."));
	
		System.out.println("Existen "+ grumosIni + " grumos.");
		int numeroUniones=numeroUniones(numUser, porcentaje, grus);
	
		if (nombreFicheroAdicional.equals("")) {
			System.out.println("Se deben unir los "+ (numeroUniones) + " mayores");

			for (int i=0; i<numeroUniones; i++) {
				double lenGrumo=grus.get(i).size();
				double porcentajeGrumo=lenGrumo/numUser*100;
				System.out.println("#"+(i+1)+": "+ (int) lenGrumo +" usuarios ("+ formato.format(porcentajeGrumo) +"%)");
			}
			
			//Impresion de nuevas relaciones y guardado en fichero
			System.out.println("Nuevas relaciones de amistad (salvadas en extra.txt)");
			for (int i=0; i<numeroUniones-1;i++) {
				salida.println(grus.get(i).get(0)+ " " + grus.get(i+1).get(1));
				System.out.println(grus.get(i).get(0)+ " <-> " + grus.get(i+1).get(1));
			}

			eliminaGrusSobrante(grus, numeroUniones);
			
		}
			
		else if (nombreFicheroAdicional.equals("extra.txt")) {
			double lenGrumo=grus.get(0).size();
			double porcentajeGrumo=lenGrumo/numUser*100;
			System.out.println("El mayor grumo contiene " + (int)lenGrumo+ " usuarios (" +  formato.format(porcentajeGrumo) + "%)");
			System.out.println("No son necesarias nuevas relaciones de amistad");
		}
		salida.close();
		in.close();
	}
	/**
	 * Recorre red buscando conexiones del usuario inicial, para sus conexiones las que tenga con otro
	 * amigo directo suyo, esas conexiones se añadiran a grumo recursivamente.
	 * @param usr_ini int que contiene el primer usuario que se añade al grumo
	 * @param red ArrayList de tipo listaUsuarios que contiene todas las conexiones entre usuarios.
	 * @param grumo ArrayList de tipo Integer en el que se guardarán las amistades correspondiendes.
	 * @return Arraylist que contendra el grumo.
	 */
	public static ArrayList<Integer> uber_amigos(int usr_ini, ArrayList<listaUsuarios> red, ArrayList<Integer> grumo) {
		grumo.add(usr_ini);
		for (int i=0; i<red.size();i++) {
				int usr1 = red.get(i).getUsr1();
				int usr2 = red.get(i).getUsr2();
				
				if ((usr_ini==usr1) && (!grumo.contains(usr2))) {
					uber_amigos(usr2,red,grumo);
				}
				if ((usr_ini==usr2) && (!grumo.contains(usr1))) {
					uber_amigos(usr1,red,grumo);
				}
		}
		return grumo;
	}
	/**
	 * Creacion de la lista red mediante la lectura de fichero.
	 * @param redes	Scanner.
	 * @param red ArrayList de tipo listaUsuarios que contiene todas las conexiones entre usuarios.
	 */
	public static void crearRed(Scanner redes, ArrayList<listaUsuarios> red) {
		
		while (redes.hasNextLine()) {
			listaUsuarios user = new listaUsuarios();
			user.setUsr1(redes.nextInt());
			user.setUsr2(redes.nextInt());
			red.add(user);

		}
	}
	
	/**
	 * Adición de las conexiones extra del fichero extra.
	 * @param extra Scanner del fichero adicional.
	 * @param red ArrayList de tipo listaUsuarios que contiene todas las conexiones entre usuarios.
	 * @param nombreFicheroAdicional nombre del fichero que se debe de llamar extra.txt
	 */
	public static void listaExtra(Scanner extra, ArrayList<listaUsuarios> red, String nombreFicheroAdicional, int numConex) {
		if (nombreFicheroAdicional.equals("extra.txt")) {
			while (extra.hasNextInt()) {
				listaUsuarios user = new listaUsuarios();
				user.setUsr1(extra.nextInt());
				user.setUsr2(extra.nextInt());
				red.add(user);
			}
		}
	}
	/**
	 * 
	 * @param extra Scanner del fichero adicional.
	 * @param nombreFicheroAdicional nombre del fichero que se debe de llamar extra.txt
	 * @return nuevasConex entero con el numero de nuevas conexiones que habra si las hay en extra.txt
	 */
	public static int nuevasConexiones(Scanner extra, String nombreFicheroAdicional) {
		int nuevasConex=0;
		if (nombreFicheroAdicional.equals("extra.txt")) {
			while (extra.hasNextLine()) {
				extra.nextLine();
				nuevasConex++;
			}
		}
		
		return nuevasConex;
	}
	/**
	 * Creación de la lista usr con los identificadores de los usuarios, recorriendo red
	 * y añadiendo los usuarios que aun no esten en usr.
	 * @param usr ArrayList de tipo Integer en el que se guardarán los usuarios.
	 * @param red ArrayList de tipo listaUsuarios que guardará todas las conexiones entre usuarios.
	 * @param numConex int con el numero de conexiones totales.
	 */
	public static void listaUsr(ArrayList<Integer> usr, ArrayList<listaUsuarios> red, int numConex) {
	
		for(int i = 0; i<numConex;i++) {
			int usr1 = red.get(i).getUsr1();
			int usr2 = red.get(i).getUsr2();
			if (!usr.contains(usr1))
				usr.add(usr1);
			else if (!usr.contains(usr2))
				usr.add(usr2);
		}
	}
	/**
	 * Creacion lista grus, es una lista compuesta de grumos, se recorren 
	 * todos los usuarios de usr y si no esta en asig se añadira a la 
	 * lista grus el grumo al que pertenece el usr.
	 * @param usr ArrayList de tipo Integer en el que están los usuarios.
	 * @param red ArrayList de tipo listaUsuarios que contiene todas las conexiones entre usuarios.
	 * @param asig ArrayList de tipo Integer que se usará como lista que almacene los usuarios con grumo asignado.
	 * @param grus ArrayList de tipo Integer en la que se añadiran los grumos.
	 */
	public static void listaGrus(ArrayList<Integer> usr, ArrayList<listaUsuarios> red, ArrayList<Integer> asig, ArrayList<ArrayList<Integer>> grus) {
		for (int i=0; i<usr.size();i++) {
			int numeroGrumo=usr.get(i);
			ArrayList<Integer> grumo = new ArrayList<>();
			if ((!asig.contains(numeroGrumo))) {
				grus.add(uber_amigos(numeroGrumo,red,grumo));
				asig.addAll(grumo);
			}

		}
	}
	
	/**
	 * Calcula el numero de uniones necesarias para poder tener un grumo mas grande que
	 * contenga el porcentaje solicitado. 
	 * @param numUser int con el numero de usuarios.
	 * @param porcentaje double con el tamaño en porcentaje del mayor grumo deseado.
	 * @param grus ArrayList de tipo Integer que contiene los grumos.
	 * @return int con el numero de uniones entre grumos
	 */
	
	public static int numeroUniones(int numUser, double porcentaje, ArrayList<ArrayList<Integer>> grus) {
		int numeroUniones = 0;
		double tamaño=0;
		for(int i=0;i<grus.size();i++) {
			if (tamaño/numUser*100<porcentaje) {
				tamaño+=grus.get(i).size();
				numeroUniones=numeroUniones+1;
			}
		}
		return numeroUniones;
		
		
	}
	/**
	 * Eliminacion del grumo que sobra una vez se ha unido una copia del mismo al inicial
	 * @param grus ArrayList de tipo Integer que contiene los grumos.
	 * @param numeroUniones int con el numero de uniones entre grumos.
	 */
	public static void eliminaGrusSobrante(ArrayList<ArrayList<Integer>> grus, int numeroUniones) {
		int k=0;
		for (int i=0;i<(numeroUniones-1);i++) {
			if(k<numeroUniones) {
				grus.get(0).addAll(grus.get(1));
				grus.remove(1);
				k++;
			}
		}
	}
	
}


