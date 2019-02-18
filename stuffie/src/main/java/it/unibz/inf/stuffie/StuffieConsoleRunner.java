package it.unibz.inf.stuffie;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Scanner;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

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
		validModesAndVals.deleteCharAt(validModesAndVals.length()-1);
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
			cls = (Class<Enum>) Class.forName("it.inf.unibz.stuffie.Mode$" + mode[0]);
			System.out.println("Succesfully changed " + mode[0] + " to " + mode[1]);
			return (Mode) Enum.valueOf(cls, mode[1]);
		} catch (ClassNotFoundException e) {
			System.out.println("Mode not found: " + mode[0] + ". The accepted modes are: " + validModes.toString() + "\n");
		} catch (IllegalArgumentException e) {
			System.out.println("Failed to change mode: " + mode[0] + ". Value not found: " + mode[1]
					+ ". The valid values are: " + validVals.get(mode[0]) + "\n");
		}

		return null;
	}

	private static void print(String str)
	{
		System.out.println(str);
	}

	private static void run_on_file(String file, Stuffie stuffie)
	{
		BufferedReader reader;
		int num_of_exceptions = 0;
		try 
		{
			reader = new BufferedReader(new FileReader(file));
			String line = reader.readLine().trim();
			if(line.charAt(line.length() - 1) != '.')
				line = line + ".";

			while (line != null) 
			{
				try
				{
					line = reader.readLine().trim();
					System.out.println(stuffie.parseRelation(line));
				}
				catch (Exception e)
				{
					print(line);
					num_of_exceptions ++;
				}
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		print("Number of Exceptions: " + Integer.toString(num_of_exceptions));
	}

	public static void main(String[] args) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException, IOException {

		initCommands();
		Mode[] modes = getCustomModes(args);
		Stuffie stuffie = new Stuffie(modes);

		Scanner reader = new Scanner(System.in);
		String text = "";
		while (!text.equals("q")) {
			System.out.println("Enter text to extract, or <h> for help: ");
			text = reader.nextLine();
			if(text.isEmpty()) {
				System.out.println("Empty line. Please try again.");
			}
			else if(text.substring(0, 3).equals("<f>")){
				print("Reading text from file and Running stuffIE over lines..");
				String file = text.substring(3).trim();
				run_on_file(file, stuffie);
			}
			else if(text.charAt(0) == '<' && text.charAt(text.length() - 1) == '>') {
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
