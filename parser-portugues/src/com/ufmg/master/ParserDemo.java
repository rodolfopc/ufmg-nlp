package com.ufmg.master;

import java.io.FileFilter;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.io.NumberRangeFileFilter;
import edu.stanford.nlp.io.NumberRangesFileFilter;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.ExactGrammarCompactor;
import edu.stanford.nlp.parser.lexparser.GrammarCompactor;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.Test;
import edu.stanford.nlp.parser.lexparser.Train;
import edu.stanford.nlp.parser.lexparser.TreebankLangParserParams;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.Numberer;

public class ParserDemo {

	private static int trainLengthLimit = 100000;

	public static void main(String[] args) {

		/* @formatter:off */
		// , "-outputFormat", "oneline"
		args = new String[] { "-tokenized", "-sentences", "newline", "-uwModel",
				"edu.stanford.nlp.parser.lexparser.BaseUnknownWordModel",
				"/home/gustavo/Mestrado/NLP/sandbox2/stanford-parser-2010-11-30/cintil.ser.gz",
				"/home/gustavo/Mestrado/NLP/sandbox2/stanford-parser-2010-11-30/portsent.txt" };

		/* @formatter:on */
		boolean train = false;
		boolean saveToSerializedFile = false;
		boolean saveToTextFile = false;
		String serializedInputFileOrUrl = null;
		String textInputFileOrUrl = null;
		String serializedOutputFileOrUrl = null;
		String textOutputFileOrUrl = null;
		String treebankPath = null;
		Treebank testTreebank = null;
		Treebank tuneTreebank = null;
		String testPath = null;
		FileFilter testFilter = null;
		String tunePath = null;
		FileFilter tuneFilter = null;
		FileFilter trainFilter = null;
		String secondaryTreebankPath = null;
		double secondaryTreebankWeight = 1.0;
		FileFilter secondaryTrainFilter = null;

		// variables needed to process the files to be parsed
		TokenizerFactory<? extends HasWord> tokenizerFactory = null;
		String tokenizerOptions = null;
		String tokenizerFactoryClass = null;
		DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor();
		boolean tokenized = false; // whether or not the input file has already
									// been tokenized
		Function<List<HasWord>, List<HasWord>> escaper = null;
		int tagDelimiter = -1;
		String sentenceDelimiter = null;
		String elementDelimiter = null;
		int argIndex = 0;
		if (args.length < 1) {
			System.err.println(
					"Basic usage (see Javadoc for more): java edu.stanford.nlp.parser.lexparser.LexicalizedParser parserFileOrUrl filename*");
			return;
		}

		Options op = new Options();
		String encoding = null;
		// while loop through option arguments
		while (argIndex < args.length && args[argIndex].charAt(0) == '-') {
			if (args[argIndex].equalsIgnoreCase("-train") || args[argIndex].equalsIgnoreCase("-trainTreebank")) {
				train = true;
				int numSubArgs = LexicalizedParser.numSubArgs(args, argIndex);
				argIndex++;
				if (numSubArgs >= 1) {
					treebankPath = args[argIndex];
					argIndex++;
				} else {
					throw new RuntimeException("Error: -train option must have treebankPath as first argument.");
				}
				if (numSubArgs == 2) {
					trainFilter = new NumberRangesFileFilter(args[argIndex++], true);
				} else if (numSubArgs >= 3) {
					try {
						int low = Integer.parseInt(args[argIndex]);
						int high = Integer.parseInt(args[argIndex + 1]);
						trainFilter = new NumberRangeFileFilter(low, high, true);
						argIndex += 2;
					} catch (NumberFormatException e) {
						// maybe it's a ranges expression?
						trainFilter = new NumberRangesFileFilter(args[argIndex], true);
						argIndex++;
					}
				}
			} else if (args[argIndex].equalsIgnoreCase("-train2")) {
				// train = true; // cdm july 2005: should require -train for
				// this
				int numSubArgs = LexicalizedParser.numSubArgs(args, argIndex);
				argIndex++;
				if (numSubArgs < 2) {
					throw new RuntimeException("Error: -train2 <treebankPath> [<ranges>] <weight>.");
				}
				secondaryTreebankPath = args[argIndex++];
				secondaryTrainFilter = numSubArgs == 3 ? new NumberRangesFileFilter(args[argIndex++], true) : null;
				secondaryTreebankWeight = Double.parseDouble(args[argIndex++]);
			} else if (args[argIndex].equalsIgnoreCase("-tLPP") && argIndex + 1 < args.length) {
				try {
					op.tlpParams = (TreebankLangParserParams) Class.forName(args[argIndex + 1]).newInstance();
				} catch (ClassNotFoundException e) {
					System.err.println("Class not found: " + args[argIndex + 1]);
				} catch (InstantiationException e) {
					System.err.println("Couldn't instantiate: " + args[argIndex + 1] + ": " + e.toString());
				} catch (IllegalAccessException e) {
					System.err.println("Illegal access" + e);
				}
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-encoding")) {
				// sets encoding for TreebankLangParserParams
				// redone later to override any serialized parser one read in
				encoding = args[argIndex + 1];
				op.tlpParams.setInputEncoding(encoding);
				op.tlpParams.setOutputEncoding(encoding);
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-tokenized")) {
				tokenized = true;
				argIndex += 1;
			} else if (args[argIndex].equalsIgnoreCase("-escaper")) {
				try {
					escaper = (Function<List<HasWord>, List<HasWord>>) Class.forName(args[argIndex + 1]).newInstance();
				} catch (Exception e) {
					System.err.println("Couldn't instantiate escaper " + args[argIndex + 1] + ": " + e);
				}
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-tokenizerOptions")) {
				tokenizerOptions = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-tokenizerFactory")) {
				tokenizerFactoryClass = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-sentences")) {
				sentenceDelimiter = args[argIndex + 1];
				if (sentenceDelimiter.equalsIgnoreCase("newline")) {
					sentenceDelimiter = "\n";
				}
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-parseInside")) {
				elementDelimiter = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-tagSeparator")) {
				tagDelimiter = args[argIndex + 1].charAt(0);
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-loadFromSerializedFile")) {
				// load the parser from a binary serialized file
				// the next argument must be the path to the parser file
				serializedInputFileOrUrl = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-loadFromTextFile")) {
				// load the parser from declarative text file
				// the next argument must be the path to the parser file
				textInputFileOrUrl = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-saveToSerializedFile")) {
				saveToSerializedFile = true;
				if (LexicalizedParser.numSubArgs(args, argIndex) < 1) {
					System.err.println("Missing path: -saveToSerialized filename");
				} else {
					serializedOutputFileOrUrl = args[argIndex + 1];
				}
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-saveToTextFile")) {
				// save the parser to declarative text file
				saveToTextFile = true;
				textOutputFileOrUrl = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-saveTrainTrees")) {
				// save the training trees to a binary file
				Train.trainTreeFile = args[argIndex + 1];
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-trainLength")) {
				// train on only short sentences
				trainLengthLimit = Integer.parseInt(args[argIndex + 1]);
				argIndex += 2;
			} else if (args[argIndex].equalsIgnoreCase("-lengthNormalization")) {
				Test.lengthNormalization = true;
				argIndex++;
			} else if (args[argIndex].equalsIgnoreCase("-treebank") || args[argIndex].equalsIgnoreCase("-testTreebank")
					|| args[argIndex].equalsIgnoreCase("-test")) {
				// the next arguments are the treebank path and maybe the range
				// for testing
				int numSubArgs = LexicalizedParser.numSubArgs(args, argIndex);
				if (numSubArgs > 0 && numSubArgs < 3) {
					argIndex++;
					testPath = args[argIndex++];
					if (numSubArgs == 2) {
						testFilter = new NumberRangesFileFilter(args[argIndex++], true);
					} else if (numSubArgs == 3) {
						try {
							int low = Integer.parseInt(args[argIndex]);
							int high = Integer.parseInt(args[argIndex + 1]);
							testFilter = new NumberRangeFileFilter(low, high, true);
							argIndex += 2;
						} catch (NumberFormatException e) {
							// maybe it's a ranges expression?
							testFilter = new NumberRangesFileFilter(args[argIndex++], true);
						}
					}
				} else {
					throw new IllegalArgumentException("Bad arguments after -testTreebank");
				}
			} else if (args[argIndex].equalsIgnoreCase("-tune")) {
				// the next argument is the treebank path and range for tuning
				int numSubArgs = LexicalizedParser.numSubArgs(args, argIndex);
				argIndex++;
				if (numSubArgs == 1) {
					tuneFilter = new NumberRangesFileFilter(args[argIndex++], true);
				} else if (numSubArgs > 1) {
					tunePath = args[argIndex++];
					if (numSubArgs == 2) {
						tuneFilter = new NumberRangesFileFilter(args[argIndex++], true);
					} else if (numSubArgs >= 3) {
						try {
							int low = Integer.parseInt(args[argIndex]);
							int high = Integer.parseInt(args[argIndex + 1]);
							tuneFilter = new NumberRangeFileFilter(low, high, true);
							argIndex += 2;
						} catch (NumberFormatException e) {
							// maybe it's a ranges expression?
							tuneFilter = new NumberRangesFileFilter(args[argIndex++], true);
						}
					}
				}
			} else {
				argIndex = op.setOptionOrWarn(args, argIndex);
			}
		} // end while loop through arguments

		// set up tokenizerFactory with options if provided
		if (tokenizerFactoryClass != null || tokenizerOptions != null) {
			try {
				if (tokenizerFactoryClass != null) {
					Class<TokenizerFactory<? extends HasWord>> clazz = (Class<TokenizerFactory<? extends HasWord>>) Class
							.forName(tokenizerFactoryClass);
					Method factoryMethod;
					if (tokenizerOptions != null) {
						factoryMethod = clazz.getMethod("newWordTokenizerFactory", String.class);
						tokenizerFactory = (TokenizerFactory<? extends HasWord>) factoryMethod.invoke(null,
								tokenizerOptions);
					} else {
						factoryMethod = clazz.getMethod("newTokenizerFactory");
						tokenizerFactory = (TokenizerFactory<? extends HasWord>) factoryMethod.invoke(null);
					}
				} else {
					// have options but no tokenizer factory; default to PTB
					tokenizerFactory = PTBTokenizer.PTBTokenizerFactory.newWordTokenizerFactory(tokenizerOptions);
				}
			} catch (Exception e) {
				System.err.println("Couldn't instantiate TokenizerFactory " + tokenizerFactoryClass + " with options "
						+ tokenizerOptions);
				e.printStackTrace();
			}
		}

		// all other arguments are order dependent and
		// are processed in order below

		if (tuneFilter != null) {
			if (tunePath == null) {
				if (treebankPath == null) {
					throw new RuntimeException("No tune treebank path specified...");
				} else {
					System.err.println("No tune treebank path specified.  Using train path: \"" + treebankPath + '\"');
					tunePath = treebankPath;
				}
			}
			tuneTreebank = op.tlpParams.testMemoryTreebank();
			tuneTreebank.loadPath(tunePath, tuneFilter);
		}

		if (!train && Test.verbose) {
			System.err.println("Currently " + new Date());
			LexicalizedParser.printArgs(args, System.err);
		}
		LexicalizedParser lp = null;
		if (train) {
			LexicalizedParser.printArgs(args, System.err);
			// so we train a parser using the treebank
			GrammarCompactor compactor = null;
			if (Train.compactGrammar() == 3) {
				compactor = new ExactGrammarCompactor(false, false);
			}
			Treebank trainTreebank = LexicalizedParser.makeTreebank(treebankPath, op, trainFilter);
			if (secondaryTreebankPath != null) {
				DiskTreebank secondaryTrainTreebank = LexicalizedParser.makeSecondaryTreebank(secondaryTreebankPath, op,
						secondaryTrainFilter);
				lp = new LexicalizedParser(trainTreebank, secondaryTrainTreebank, secondaryTreebankWeight, compactor,
						op);
			} else {
				lp = new LexicalizedParser(trainTreebank, compactor, op, tuneTreebank);
			}
		} else if (textInputFileOrUrl != null) {
			// so we load the parser from a text grammar file
			lp = new LexicalizedParser(textInputFileOrUrl, true, op);
		} else {
			// so we load a serialized parser
			if (serializedInputFileOrUrl == null && argIndex < args.length) {
				// the next argument must be the path to the serialized parser
				serializedInputFileOrUrl = args[argIndex];
				argIndex++;
			}
			if (serializedInputFileOrUrl == null) {
				System.err.println("No grammar specified, exiting...");
				return;
			}
			try {
				lp = new LexicalizedParser(serializedInputFileOrUrl, op);
				op = lp.op;
			} catch (IllegalArgumentException e) {
				System.err.println("Error loading parser, exiting...");
				return;
			}
		}

		// the following has to go after reading parser to make sure
		// op and tlpParams are the same for train and test
		// THIS IS BUTT UGLY BUT IT STOPS USER SPECIFIED ENCODING BEING
		// OVERWRITTEN BY ONE SPECIFIED IN SERIALIZED PARSER
		if (encoding != null) {
			op.tlpParams.setInputEncoding(encoding);
			op.tlpParams.setOutputEncoding(encoding);
		}

		if (testFilter != null || testPath != null) {
			if (testPath == null) {
				if (treebankPath == null) {
					throw new RuntimeException("No test treebank path specified...");
				} else {
					System.err.println("No test treebank path specified.  Using train path: \"" + treebankPath + '\"');
					testPath = treebankPath;
				}
			}
			testTreebank = op.tlpParams.testMemoryTreebank();
			testTreebank.loadPath(testPath, testFilter);
		}

		Train.sisterSplitters = new HashSet<String>(Arrays.asList(op.tlpParams.sisterSplitters()));

		// at this point we should be sure that op.tlpParams is
		// set appropriately (from command line, or from grammar file),
		// and will never change again. -- Roger

		// Now what do we do with the parser we've made
		if (saveToTextFile) {
			// save the parser to textGrammar format
			if (textOutputFileOrUrl != null) {
				LexicalizedParser.saveParserDataToText(lp.pd, textOutputFileOrUrl);
			} else {
				System.err.println("Usage: must specify a text grammar output path");
			}
		}
		if (saveToSerializedFile) {
			if (serializedOutputFileOrUrl != null) {
				LexicalizedParser.saveParserDataToSerialized(lp.pd, serializedOutputFileOrUrl);
			} else if (textOutputFileOrUrl == null && testTreebank == null) {
				// no saving/parsing request has been specified
				System.err.println("usage: " + "java edu.stanford.nlp.parser.lexparser.LexicalizedParser "
						+ "-train trainFilesPath [fileRange] -saveToSerializedFile serializedParserFilename");
			}
		}

		if (Test.verbose || train) {
			// Tell the user a little or a lot about what we have made
			// get lexicon size separately as it may have it's own prints in
			// it....
			String lexNumRules = lp.pparser != null ? Integer.toString(lp.pparser.lex.numRules()) : "";
			System.err.println("Grammar\tStates\tTags\tWords\tUnaryR\tBinaryR\tTaggings");
			System.err.println("Grammar\t" + Numberer.getGlobalNumberer("states").total() + '\t'
					+ Numberer.getGlobalNumberer("tags").total() + '\t' + Numberer.getGlobalNumberer("words").total()
					+ '\t' + (lp.pparser != null ? lp.pparser.ug.numRules() : "") + '\t'
					+ (lp.pparser != null ? lp.pparser.bg.numRules() : "") + '\t' + lexNumRules);
			System.err.println("ParserPack is " + op.tlpParams.getClass().getName());
			System.err.println("Lexicon is " + lp.pd.lex.getClass().getName());
			if (Test.verbose) {
				System.err.println("Tags are: " + Numberer.getGlobalNumberer("tags"));
				// System.err.println("States are: " +
				// Numberer.getGlobalNumberer("states")); // This is too
				// verbose. It was already printed out by the below printOptions
				// command if the flag -printStates is given!
			}
			LexicalizedParser.printOptions(false, op);
		}

		if (testTreebank != null) {
			// test parser on treebank
			lp.testOnTreebank(testTreebank);
		} else if (argIndex >= args.length) {
			// no more arguments, so we just parse our own test sentence
			PrintWriter pwOut = op.tlpParams.pw();
			PrintWriter pwErr = op.tlpParams.pw(System.err);
			if (lp.parse(op.tlpParams.defaultTestSentence())) {
				Test.treePrint(op.tlpParams).printTree(lp.getBestParse(), pwOut);
			} else {
				pwErr.println("Error. Can't parse test sentence: " + op.tlpParams.defaultTestSentence());
			}
		} else {
			// We parse filenames given by the remaining arguments
			lp.parseFiles(args, argIndex, tokenized, tokenizerFactory, documentPreprocessor, elementDelimiter,
					sentenceDelimiter, escaper, tagDelimiter);
		}
	} // end
		// main

	// public static void main(String[] args) throws IOException {
	// String pathname =
	// "/home/gustavo/Mestrado/NLP/sandbox2/stanford-parser-2010-11-30/cintil.ser.gz";
	// LexicalizedParser lp = new LexicalizedParser(pathname);
	//
	// lp.setOptionFlags(new String[] { "-maxLength", "80",
	// "-retainTmpSubcategories" });
	//
	// String[] sent = "Conselho deixa para amanhã votação de processo de
	// Cunha".split(" ");
	// Tree parse = lp.apply(Arrays.asList(sent));
	// parse.pennPrint();
	// System.out.println();
	//
	// TreebankLanguagePack tlp = new PennTreebankLanguagePack();
	// GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
	//
	// GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
	// Collection tdl = gs.typedDependenciesCollapsed();
	// System.out.println(tdl);
	// System.out.println();
	//
	// TreePrint tp = new TreePrint("penn,typedDependenciesCollapsed");
	// tp.printTree(parse);
	// }
}
