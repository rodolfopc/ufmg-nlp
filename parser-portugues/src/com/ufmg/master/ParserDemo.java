package com.ufmg.master;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.parser.lexparser.Options;
import edu.stanford.nlp.parser.lexparser.Train;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.Function;

public class ParserDemo {

	public static void main(String[] args) {

		/* @formatter:off */
		// , "-outputFormat", "oneline","-tokenized", "-sentences", "newline",
		// "-uwModel", "edu.stanford.nlp.parser.lexparser.BaseUnknownWordModel",
		args = new String[] { "/home/gustavo/Mestrado/NLP/sandbox2/stanford-parser-2010-11-30/cintil.ser.gz",
				"/home/gustavo/Mestrado/NLP/sandbox2/stanford-parser-2010-11-30/portsent.txt" };

		/* @formatter:on */
		String serializedInputFileOrUrl = null;

		// variables needed to process the files to be parsed
		TokenizerFactory<? extends HasWord> tokenizerFactory = null;
		DocumentPreprocessor documentPreprocessor = new DocumentPreprocessor();
		boolean tokenized = true; // whether or not the input file has already
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

		sentenceDelimiter = "\n";

		serializedInputFileOrUrl = args[0];
		argIndex++;

		LexicalizedParser lp = null;
		try {
			lp = new LexicalizedParser(serializedInputFileOrUrl, op);
			op = lp.op;
		} catch (IllegalArgumentException e) {
			System.err.println("Error loading parser, exiting...");
			return;
		}
		Train.sisterSplitters = new HashSet<String>(Arrays.asList(op.tlpParams.sisterSplitters()));

		lp.parseFiles(args, argIndex, tokenized, tokenizerFactory, documentPreprocessor, elementDelimiter,
				sentenceDelimiter, escaper, tagDelimiter);

		ArrayList<Tree> trees = lp.getTrees();
		for (Tree tree : trees) {
			System.out.println(tree.toString());
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
