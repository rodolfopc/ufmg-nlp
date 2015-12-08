package com.ufmg.masters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.MemoryTreebank;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;
import edu.stanford.nlp.trees.TreeReaderFactory;
import edu.stanford.nlp.trees.Treebank;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.tregex.Macros;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.trees.tregex.TregexPattern.TRegexTreeVisitor;
import edu.stanford.nlp.trees.tregex.TregexPatternCompiler;

public class MyDemo {

	public static void main(String[] args) throws IOException {

		BufferedReader readTree = new BufferedReader(new FileReader("../arquivos/file.txt"));
		String tree = readTree.readLine();
		
		BufferedReader gIn = new BufferedReader(new FileReader("../arquivos/qtregexTemplate_pt.txt"));
		String line = gIn.readLine();
		boolean achouTregex = false;
		String template = null;
		while (line != null && template==null) {
		    if(line.contains("#")){
		    	line = line.replace("#", "");
				List<Tree> trees = demoTregex(line, tree);
				if(!trees.isEmpty()){
					achouTregex = true;
				}
		    } else {
		    	if(achouTregex) {
		    		template = line;
		    	} 
		    }
		    line = gIn.readLine();
			} 
		
		System.out.print(template);
			
		
		// String tregexCorpus =
		// "/home/gustavo/Mestrado/NLP/sandbox/tregex-semantic-tagger/tregex_corpus.en";

//		LexicalizedParser lexicalParser = new LexicalizedParser(
//				"../arquivos/englishPCFG.ser.gz");
//
//		String filename = "../arquivos/testsent.txt";
		// String filename =
		// "/home/gustavo/Mestrado/NLP/sandbox/tregex-semantic-tagger/tools/stanford-parser/data/portsent.txt";

//		String treesString = demoDP(lexicalParser, filename);
		// System.out.println(treesString);

//		String test = "(ROOT (S (NP (N João)) (VP (V é) (A feliz.))))";

//		demoTregex(matchString, line);
	}

	private static List<Tree> demoTregex(String matchString, String treesString) throws UnsupportedEncodingException {
		String encoding = "UTF-8";
		StringBuilder treePrintFormats = new StringBuilder();
		treePrintFormats.append("penn,");
		TRegexTreeVisitor.tp = new TreePrint(treePrintFormats.toString(), new PennTreebankLanguagePack());

		HeadFinder hf = new CollinsHeadFinder();
		String macroFilename = "";

		TregexPatternCompiler tpc = new TregexPatternCompiler(hf);
		Macros.addAllMacros(tpc, macroFilename, encoding);
		TregexPattern tregexPattern = tpc.compile(matchString);
		PrintWriter errPW = new PrintWriter(new OutputStreamWriter(System.err, encoding), true);
		errPW.println("Pattern string:\n" + tregexPattern.pattern());
		errPW.println("Parsed representation:");
		tregexPattern.prettyPrint(errPW);

		String[] handles = null;
		Treebank treebank;

		TreeReaderFactory trf = TregexPattern.getTreeReaderFactory(null);
		treebank = new MemoryTreebank(trf, encoding);

		// read from stdin
		// Reader reader = new BufferedReader(new InputStreamReader(System.in,
		// encoding));
		((MemoryTreebank) treebank).load(new StringReader(treesString));

		TRegexTreeVisitor vis = new TRegexTreeVisitor(tregexPattern, handles, encoding);

		treebank.apply(vis);
		System.out.println(treebank.textualSummary());

		errPW.println("There were " + vis.numMatches() + " matches in total.");
		List<Tree> matchedTrees = vis.getMatchedTrees();
		for (Tree tree : matchedTrees) {
			System.out.println(tree.nodeString());
		}
		return matchedTrees;
	}

	private static String demoDP(LexicalizedParser lp, String filename) {
		StringBuilder builder = new StringBuilder();

		TreebankLanguagePack tlp = new PennTreebankLanguagePack();
		GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
		// You could also create a tokenier here (as below) and pass it
		// to DocumentPreprocessor
		for (List<HasWord> sentence : new DocumentPreprocessor(filename)) {
			Tree parse = lp.apply(sentence);
			// parse.pennPrint();
			String ts = parse.pennString();
			builder.append(ts);

			GrammaticalStructure gs = gsf.newGrammaticalStructure(parse);
			Collection tdl = gs.typedDependenciesCCprocessed(true);
			// System.out.println(tdl);
			// System.out.println();
		}
		return builder.toString();
	}
}
