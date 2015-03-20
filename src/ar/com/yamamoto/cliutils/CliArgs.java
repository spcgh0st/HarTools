package ar.com.yamamoto.cliutils;

import java.util.HashMap;

public class CliArgs {
	private String[] rawArgs;
	private HashMap<String, Object> argsMap;
	private HashMap<Character, String> argsTrans;
	
	/**
	 * Constructor
	 * @param args
	 * 		Raw arguments received from CLI
	 */
	public CliArgs(String[] args) {
		init(args, new HashMap<Character, String>());
	}
	
	/**
	 * Constructor
	 * @param args
	 * 		Raw arguments received from CLI
	 * @param argsTrans
	 * 		Arguments translations between single/multiple
	 * 		characters
	 */
	public CliArgs(String[] args, HashMap<Character, String>argsTrans) {
		init(args, argsTrans);
	}
	
	/**
	 * Constructor helper
	 * @param args
	 * @param argsTrans
	 * 		Instantiate an empty HashMap if translation not received
	 * 		in object initialization
	 */
	private void init(String[]args, HashMap<Character, String>argsTrans) {
		this.argsTrans = argsTrans;
		rawArgs = new String[args.length];
		System.arraycopy(args, 0, rawArgs, 0, args.length);
		
		argsMap = new HashMap<String, Object>();
		this.loadArgs();
	}
	
	/**
	 * Process String[] of args into HashMap
	 */
	private void loadArgs() {
		for(int a = 0; a < rawArgs.length; a++) {
			String argName = null;
			
			if(rawArgs[a].startsWith("--")) {
				argName = rawArgs[a].substring(2);
			} else if(rawArgs[a].startsWith("-")) {
				if(argsTrans.containsKey(rawArgs[a].charAt(1))) {
					argName = argsTrans.get(rawArgs[a].charAt(1));
				}
			}
			
			if(argName != null) {
				if(a < rawArgs.length - 1 && !rawArgs[a + 1].startsWith("-")) {
					argsMap.put(argName, rawArgs[a + 1]);
				} else {
					argsMap.put(argName, true);
				}
			}
		}
	}
	
	/**String
	 * Check if an argument was received,
	 * for multiple-char argument keys
	 * 
	 * @param argName
	 * 		Argument key
	 * @return
	 * 		true/false
	 */
	public boolean hasArg(String argName) {
		return argsMap.containsKey(argName);
	}
	
	/**
	 * Check if an argument was received,
	 * for single-char argument keys.
	 * Search translation to multiple-char
	 * keys, and calls multiple-char overloaded
	 * method
	 * 
	 * @param argName
	 * 		Argument key
	 * @return
	 * 		true/false
	 */
	public boolean hasArg(char argName) {
		if(argsTrans.containsKey(argName)) {
			return argsMap.containsKey(
					argsTrans.get(argName));
		} else {
			return false;
		}
	}
	
	/**
	 * Get the value received from CLI
	 * for the key received as argument
	 * 
	 * @param argName
	 * 		Argument key
	 * @return
	 * 		Argument value, or null
	 */
	public Object getValue(String argName) {
		if(this.hasArg(argName)) {
			return argsMap.get(argName);
		} else {
			return null;
		}
	}	
}
