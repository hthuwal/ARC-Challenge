package it.unibz.inf.stuffie;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Scanner;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

class MyThread implements Runnable {
	int id;
	static AtomicInteger count = new AtomicInteger(0);
	static AtomicInteger num_of_exceptions = new AtomicInteger(0);
	static AtomicInteger num_of_lines = new AtomicInteger(0);
	Stuffie stuffie;
	Thread t;
	String source_file_name;
	String source_file_path;
	String out_file_path;
	String exceptions_file_path;
	String tdir;

	MyThread(int id, String file, String tdir, Stuffie stuffie) {
		count.incrementAndGet();
		this.id = id;
		this.tdir = tdir;
		this.source_file_path = file;
		this.out_file_path = file + ".openie";
		this.exceptions_file_path = file + ".exceptions";
		this.stuffie = stuffie;
		this.source_file_name = Paths.get(file).getFileName().toString();

		t = new Thread(this, Integer.toString(id));
		System.out.println("New thread: " + t);
		t.start();
	}

	private void print(String str) {
		System.out.printf("Thread %d: %s", id, str);
	}

	public void flush(BufferedWriter bw, BufferedWriter ew, StringBuilder sb, StringBuilder eb) {
		try {
			bw.write(sb.toString());
			bw.flush();

			ew.write(eb.toString());
			ew.flush();

		} catch (IOException e) {
			print("IOException Occurred");
		}
	}

	public void move(String source, String dest) {
		Path temp;
		try {
			print(source + " " + dest);
			temp = Files.move(Paths.get(source), Paths.get(dest));
			if (temp != null) {
				print(source + " Moved successfully\n");
			} else {
				print("Failed to move the file:" + source + "\n");
			}
		} catch (IOException e) {
			print("Moving Failed\n");
		}
	}

	public void run() {
		try {

			/* ------------------------- Reading File as Stream ------------------------- */
			Stream<String> lines = Files.lines(Paths.get(this.source_file_path));
			StringBuilder sb = new StringBuilder(15000);
			StringBuilder eb = new StringBuilder(15000);
			BufferedWriter bw = new BufferedWriter(new FileWriter(this.out_file_path));
			BufferedWriter ew = new BufferedWriter(new FileWriter(this.exceptions_file_path));

			print("Running Stuffie on each line\n");

			lines.forEach(line -> {

				line = line.replaceAll("[^A-Za-z0-9 .,]", "");
				if (sb.length() > 10000 || eb.length() > 10000) {
					flush(bw, ew, sb, eb);
					sb.delete(0, sb.length());
					eb.delete(0, eb.length());

				}

				line = line.trim();
				if (!line.isEmpty() && line.charAt(line.length() - 1) != '.')
					line = line + ".";
				try {
					if (!line.isEmpty()) {
						String repr = stuffie.parseRelation(line);
						if (!repr.isEmpty()) {
							num_of_lines.incrementAndGet();
							sb.append("## ").append(line).append("\n");
							sb.append("\n" + repr + "\n");
						}
					}
				} catch (Exception e) {
					num_of_exceptions.incrementAndGet();
					eb.append(line + "\n");
				}
			});
			lines.close();
			flush(bw, ew, sb, eb);
			sb.delete(0, sb.length());
			eb.delete(0, eb.length());
			bw.close();
			ew.close();

		} catch (

		IOException e) {
			e.printStackTrace();
		}
		print("Moving Results to " + tdir + "...\n");
		move(this.source_file_path, tdir + "/" + this.source_file_name);
		move(this.out_file_path, tdir + "/" + this.source_file_name + ".openie");
		move(this.exceptions_file_path, tdir + "/" + this.source_file_name + ".exceptions");
		System.out.println("Completed thread " + Integer.toString(id));
		count.decrementAndGet();
	}
}

public class StuffieConsoleRunner {

	private static LinkedHashMap<String, String> commands = new LinkedHashMap<>();
	private static LinkedHashMap<String, String> shorthandCommands = new LinkedHashMap<>();
	private static LinkedHashMap<String, String> loweredKeyCommands = new LinkedHashMap<>();
	private static StringBuilder validModes = new StringBuilder();
	private static StringBuilder validModesAndVals = new StringBuilder();
	private static LinkedHashMap<String, String> validVals = new LinkedHashMap<>();

	private static void initCommands() throws IOException {

		try (BufferedReader br = Files.newBufferedReader(Paths.get("resource/console_commands.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] command = line.split("-");
				commands.put(command[0], command[1]);
				loweredKeyCommands.put(command[0].toLowerCase(), command[0]);
				if (command.length > 2)
					shorthandCommands.put(command[1], command[2]);
			}
		}

		for (Class<?> x : Mode.class.getClasses()) {
			validModesAndVals.append("\t\t" + x.getSimpleName() + "=[");
			validModes.append(x.getSimpleName() + ", ");
			StringBuilder vals = new StringBuilder();
			for (Object enumval : x.getEnumConstants()) {
				vals.append(enumval.toString()).append(", ");
				validModesAndVals.append(enumval.toString()).append("|");
			}
			vals.deleteCharAt(vals.length() - 1).deleteCharAt(vals.length() - 1).append(".");
			validVals.put(x.getSimpleName(), vals.toString());
			validModesAndVals.deleteCharAt(validModesAndVals.length() - 1).append("]\n");
		}
		validModes.deleteCharAt(validModes.length() - 1).deleteCharAt(validModes.length() - 1).append(".");
		validModesAndVals.deleteCharAt(validModesAndVals.length() - 1);
	}

	private static Mode[] getCustomModes(String[] args) {
		Mode[] modes = new Mode[args.length];

		int i = 0;
		for (String arg : args) {
			Mode m = getValidMode(arg);
			if (m == null)
				System.exit(1);
			modes[i] = getValidMode(arg);
			i++;
		}

		return modes;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Mode getValidMode(String arg) {
		String[] mode = arg.split("=");

		if (mode.length != 2) {
			System.out.println("Invalid mode change command: " + arg + ".");
		}

		Class<Enum> cls;
		try {
			cls = (Class<Enum>) Class.forName("it.unibz.inf.stuffie.Mode$" + mode[0]);
			System.out.println("Succesfully changed " + mode[0] + " to " + mode[1]);
			return (Mode) Enum.valueOf(cls, mode[1]);
		} catch (ClassNotFoundException e) {
			System.out.println(
					"Mode not found: " + mode[0] + ". The accepted modes are: " + validModes.toString() + "\n");
		} catch (IllegalArgumentException e) {
			System.out.println("Failed to change mode: " + mode[0] + ". Value not found: " + mode[1]
					+ ". The valid values are: " + validVals.get(mode[0]) + "\n");
		}

		return null;
	}

	private static void print(String str) {
		System.out.println(str);
	}

	private static void run_on_file(String source_dir, String finished_dir, int num_threads, String[] args)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException, InterruptedException {
		File f = new File(source_dir);
		ArrayList<File> files = new ArrayList<File>(Arrays.asList(f.listFiles()));
		Mode[] modes = getCustomModes(args);
		Mode m = getValidMode("PrintDependenyTree=DISABLED");

		for (int i = 0; i < files.size(); i++) {
			while (MyThread.count.get() >= num_threads) {
				System.out.printf("\rThreads: %d, Files: %d", MyThread.count.get(), i);
				TimeUnit.SECONDS.sleep(2);
			}

			Stuffie stuffie = new Stuffie(modes);
			stuffie.setMode(m);
			String file = files.get(i).getPath();
			new MyThread(i, file, finished_dir, stuffie);
		}
		while (MyThread.count.get() > 0) {
			String line = Integer.toString(MyThread.num_of_lines.get()) + " / 14621856, "
					+ Integer.toString(MyThread.num_of_exceptions.get()) + ", " + Integer.toString(MyThread.count.get())
					+ " threads Running";
			System.out.print("\r" + line);
			TimeUnit.SECONDS.sleep(2);
		}
	}

	public static void main(String[] args)
			throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException, IOException, InterruptedException {

		initCommands();
		Mode[] modes = getCustomModes(args);
		Stuffie stuffie = new Stuffie(modes);

		Scanner reader = new Scanner(System.in);
		String text = "";
		while (!text.equals("q")) {
			System.out.println("Enter text to extract, or <h> for help: ");
			text = reader.nextLine();

			if (text.isEmpty()) {
				System.out.println("Empty line. Please try again.");
			} else if (text.substring(0, 3).equals("<f>")) {
				print("Reading text from file and Running stuffIE over lines..");
				String[] arr = text.split(" ");
				run_on_file(arr[1], arr[2], Integer.parseInt(arr[3]), args);
				print("");
				text = "q";
			} else if (text.charAt(0) == '<' && text.charAt(text.length() - 1) == '>') {
				text = text.substring(1, text.length() - 1);
				if (text.contains("=")) {
					Mode m = getValidMode(text);
					if (m != null)
						stuffie.setMode(m);
				} else {
					String textLower = text.toLowerCase();
					if (!loweredKeyCommands.containsKey(textLower) && !shorthandCommands.containsKey(textLower)) {
						System.out.println("Invalid command: " + text + ". Enter <h> to list all valid commands.\n");
					} else if (textLower.equals("help") || textLower.equals("h")) {
						for (String command : commands.keySet()) {
							if (shorthandCommands.containsKey(commands.get(command))) {
								System.out.println("\t<" + command + "> <" + commands.get(command) + ">\t"
										+ shorthandCommands.get(commands.get(command)) + "\n");
							} else {
								System.out.println("\t<" + command + ">\t" + commands.get(command) + "\n");
							}
						}
						System.out.println(validModesAndVals.toString() + "\n");
					} else if (textLower.equals("show modes") || textLower.equals("sm")) {
						System.out.println("Current active modes: " + stuffie.currentModesInString() + ".\n");
					}
				}
			} else {
				System.out.println(stuffie.parseRelation(text));
			}
		}
		System.out.print("Bye bye.");
		reader.close();
	}
}
