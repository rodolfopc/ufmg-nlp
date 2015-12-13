package com.ufmg.masters.tregex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.StringLabelFactory;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.DiskTreebank;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.LabeledScoredTreeFactory;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.PennTreeReader;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreeReader;
import edu.stanford.nlp.trees.TreeReaderFactory;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.tregex.Macros;
import edu.stanford.nlp.trees.tregex.TregexParseException;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.TregexPattern.TRegexTreeVisitor;
import edu.stanford.nlp.trees.tregex.TregexPatternCompiler;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.Timing;

public class TregexDemo {

	public static void main(String[] args) throws IOException {
		Timing.startTime();

		args = new String[2];
		args[0] = "VP < VBD < NP";
		args[1] = "/home/gustavo/Mestrado/NLP/sandbox/tregex-semantic-tagger/tregex_corpus.en";

		StringBuilder treePrintFormats = new StringBuilder();
		String printNonMatchingTreesOption = "-v";
		String subtreeCodeOption = "-x";
		String extractSubtreesOption = "-extract";
		String extractSubtreesFileOption = "-extractFile";
		String inputFileOption = "-i";
		String headFinderOption = "-hf";
		String headFinderArgOption = "-hfArg";
		String trfOption = "-trf";
		String headFinderClassName = null;
		String[] headFinderArgs = StringUtils.EMPTY_STRING_ARRAY;
		String treeReaderFactoryClassName = null;
		String printHandleOption = "-h";
		String markHandleOption = "-k";
		String encodingOption = "-encoding";
		String encoding = "UTF-8";
		String macroOption = "-macros";
		String macroFilename = "";
		Map<String, Integer> flagMap = new HashMap<String, Integer>();
		flagMap.put(extractSubtreesOption, 2);
		flagMap.put(extractSubtreesFileOption, 2);
		flagMap.put(subtreeCodeOption, 0);
		flagMap.put(printNonMatchingTreesOption, 0);
		flagMap.put(encodingOption, 1);
		flagMap.put(inputFileOption, 1);
		flagMap.put(printHandleOption, 1);
		flagMap.put(markHandleOption, 2);
		flagMap.put(headFinderOption, 1);
		flagMap.put(headFinderArgOption, 1);
		flagMap.put(trfOption, 1);
		flagMap.put(macroOption, 1);
		Map<String, String[]> argsMap = StringUtils.argsToMap(args, flagMap);
		args = argsMap.get(null);

		if (argsMap.containsKey(encodingOption)) {
			encoding = argsMap.get(encodingOption)[0];
			System.err.println("Encoding set to " + encoding);
		}
		PrintWriter errPW = new PrintWriter(new OutputStreamWriter(System.err, encoding), true);

		if (argsMap.containsKey(extractSubtreesOption)) {
			List<String> subTreeStrings = Collections.singletonList(argsMap.get(extractSubtreesOption)[0]);
			TregexPattern.extractSubtrees(subTreeStrings, argsMap.get(extractSubtreesOption)[1]);
			return;
		}
		if (argsMap.containsKey(extractSubtreesFileOption)) {
			List<String> subTreeStrings = Arrays
					.asList(IOUtils.slurpFile(argsMap.get(extractSubtreesFileOption)[0]).split("\n|\r|\n\r"));
			TregexPattern.extractSubtrees(subTreeStrings, argsMap.get(extractSubtreesFileOption)[0]);
			return;
		}

		if (args.length < 1) {
			errPW.println(
					"Usage: java stanford.nlp.trees.tregex.TregexPattern [-T] [-C] [-w] [-f] [-o] [-n] [-s] [-filter]  [-hf class] [-trf class] [-h handle]* pattern [filepath]");
			return;
		}
		String matchString = args[0];

		if (argsMap.containsKey(macroOption)) {
			macroFilename = argsMap.get(macroOption)[0];
		}
		if (argsMap.containsKey(headFinderOption)) {
			headFinderClassName = argsMap.get(headFinderOption)[0];
			errPW.println("Using head finder " + headFinderClassName + "...");
		}
		if (argsMap.containsKey(headFinderArgOption)) {
			headFinderArgs = argsMap.get(headFinderArgOption);
		}
		if (argsMap.containsKey(trfOption)) {
			treeReaderFactoryClassName = argsMap.get(trfOption)[0];
			errPW.println("Using tree reader factory " + treeReaderFactoryClassName + "...");
		}
		if (argsMap.containsKey("-T")) {
			TRegexTreeVisitor.printTree = true;
		}
		if (argsMap.containsKey(inputFileOption)) {
			String inputFile = argsMap.get(inputFileOption)[0];
			matchString = IOUtils.slurpFile(inputFile, encoding);
			String[] newArgs = new String[args.length + 1];
			System.arraycopy(args, 0, newArgs, 1, args.length);
			args = newArgs;
		}
		if (argsMap.containsKey("-C")) {
			TRegexTreeVisitor.printMatches = false;
			TRegexTreeVisitor.printNumMatchesToStdOut = true;

		}
		if (argsMap.containsKey("-v")) {
			TRegexTreeVisitor.printNonMatchingTrees = true;
		}
		if (argsMap.containsKey("-x")) {
			TRegexTreeVisitor.printSubtreeCode = true;
			TRegexTreeVisitor.printMatches = false;
		}
		if (argsMap.containsKey("-w")) {
			TRegexTreeVisitor.printWholeTree = true;
		}
		if (argsMap.containsKey("-f")) {
			TRegexTreeVisitor.printFilename = true;
		}
		if (argsMap.containsKey("-o")) {
			TRegexTreeVisitor.oneMatchPerRootNode = true;
		}
		if (argsMap.containsKey("-n")) {
			TRegexTreeVisitor.reportTreeNumbers = true;
		}
		if (argsMap.containsKey("-u")) {
			treePrintFormats.append(TreePrint.rootLabelOnlyFormat).append(',');
		} else if (argsMap.containsKey("-s")) { // display short form
			treePrintFormats.append("oneline,");
		} else if (argsMap.containsKey("-t")) {
			treePrintFormats.append("words,");
		} else {
			treePrintFormats.append("penn,");
		}

		HeadFinder hf = new CollinsHeadFinder();
		if (headFinderClassName != null) {
			Class[] hfArgClasses = new Class[headFinderArgs.length];
			for (int i = 0; i < hfArgClasses.length; i++) {
				hfArgClasses[i] = String.class;
			}
			try {
				hf = (HeadFinder) Class.forName(headFinderClassName).getConstructor(hfArgClasses)
						.newInstance((Object[]) headFinderArgs); // cast to
																	// Object[]
																	// necessary
																	// to avoid
																	// varargs-related
																	// warning.
			} catch (Exception e) {
				throw new RuntimeException("Error occurred while constructing HeadFinder: " + e);
			}
		}

		TRegexTreeVisitor.tp = new TreePrint(treePrintFormats.toString(), new PennTreebankLanguagePack());

		try {
			// TreePattern p = TreePattern.compile("/^S/ > S=dt $++ '' $-- ``");
			TregexPatternCompiler tpc = new TregexPatternCompiler(hf);
			Macros.addAllMacros(tpc, macroFilename, encoding);
			TregexPattern p = tpc.compile(matchString);
			errPW.println("Pattern string:\n" + p.pattern());
			errPW.println("Parsed representation:");
			p.prettyPrint(errPW);

			String[] handles = argsMap.get(printHandleOption);
			Treebank treebank;
			if (argsMap.containsKey("-filter")) {
				TreeReaderFactory trf = TregexPattern.getTreeReaderFactory(treeReaderFactoryClassName);
				treebank = new MemoryTreebank(trf, encoding);// has to be in
																// memory since
																// we're not
																// storing it on
																// disk

				// read from stdin
				Reader reader = new BufferedReader(new InputStreamReader(System.in, encoding));
				((MemoryTreebank) treebank).load(reader);
				reader.close();
			} else if (args.length == 1) {
				errPW.println("using default tree");
				TreeReader r = new PennTreeReader(
						new StringReader(
								"(VP (VP (VBZ Try) (NP (NP (DT this) (NN wine)) (CC and) (NP (DT these) (NNS snails)))) (PUNCT .))"),
						new LabeledScoredTreeFactory(new StringLabelFactory()));
				Tree t = r.readTree();
				treebank = new MemoryTreebank();
				treebank.add(t);
			} else {
				int last = args.length - 1;
				errPW.println("Reading trees from file(s) " + args[last]);
				TreeReaderFactory trf = TregexPattern.getTreeReaderFactory(treeReaderFactoryClassName);
				treebank = new DiskTreebank(trf, encoding);
				treebank.loadPath(args[last], null, true);

			}
			TRegexTreeVisitor vis = new TRegexTreeVisitor(p, handles, encoding);

			treebank.apply(vis);
			Timing.endTime();
			if (TRegexTreeVisitor.printMatches) {
				errPW.println("There were " + vis.numMatches() + " matches in total.");
			}
			if (TRegexTreeVisitor.printNumMatchesToStdOut) {
				System.out.println(vis.numMatches());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TregexParseException e) {
			errPW.println("Error parsing expression: " + args[0]);
			errPW.println("Parse exception: " + e.toString());
		}

	}
}
