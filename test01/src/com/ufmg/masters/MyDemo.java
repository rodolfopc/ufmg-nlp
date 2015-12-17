package com.ufmg.masters;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.CollinsHeadFinder;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.HeadFinder;
import edu.stanford.nlp.trees.LabeledScoredTreeNode;
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
		while (tree != null) {
		
			BufferedReader gIn = new BufferedReader(new FileReader("../arquivos/qtregexTemplate_pt.txt"));
			String line = gIn.readLine();
			List<Tree> trees = null;
			boolean achouTregex = false;
			String template = null;
			while (line != null) {
			    if(line.contains("#")){
					System.out.print(line+ "\n");
			    	line = line.replace("#", "");
					trees = demoTregex(line, tree);
					if(!trees.isEmpty()){
						achouTregex = true;
					}
			    } else {
			    	if(achouTregex) {
			    		template = line;
			    		
			    		generateQuestions(template, trees);
			    	} 
			    }
			    line = gIn.readLine();
			}
			gIn.close();

			tree = readTree.readLine();
		}
		readTree.close();
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
	
	
	
	public static void generateQuestions(String template, List<Tree> trees){

		
		if(trees!=null && template!=null && !template.equals("")){
			String patternConditions = "\\[\\.(\\D+)\\.\\]";
			String pattern = "\\%\\%([A-Z]+)\\%\\%";

			String expression = patternMatcher(pattern, template);
			String conditions = patternMatcher(patternConditions, template);
		     
			String[] a = template.split(patternConditions);
			template = a[1];
			
		     String[] frases = new String[trees.size()];
			for (int i = 0; i < trees.size(); i++) {
				Object[] array = trees.get(i).toArray();

				String[] phrase = new String[trees.get(i).getLeaves().size()];
				StringBuilder st = new StringBuilder();
				for (int x = 0; x < trees.get(i).getLeaves().size(); x++) {
					phrase[x] = trees.get(i).getLeaves().get(x).value();
					st.append(trees.get(i).getLeaves().get(x).value()+" ");
				}
				
				if(checkConditions(conditions,phrase)){
				
				      if(expression!=null){
				    	  LabeledScoredTreeNode selectedNode = null;
							for (int j = 0; j < array.length; j++) {
								LabeledScoredTreeNode labeledScoredTreeNode = (LabeledScoredTreeNode) array[j];
								Label label = labeledScoredTreeNode.label();
								String value = label.value();
								if(value.equals(expression)){
									selectedNode = labeledScoredTreeNode;
									break;
								}
							}
							StringBuilder sb = new StringBuilder();

							for (int j = 0; j < selectedNode.getLeaves().size(); j++) {
								sb.append(selectedNode.getLeaves().get(j).value()+" ");
							}
							frases[i] = sb.toString();
							String pergunta = template.replace("%%"+expression+"%%", sb.toString());
							System.out.print(pergunta+"\n");
				      }
				}
			}
		}
	}
	
	
	
	
	
	public static boolean checkConditions(String conditions, String[] phrase){
		String[] separatedConditions = conditions.split(",");
		boolean isValid = true;
		for (int i = 0; i < separatedConditions.length; i++) {
			if(separatedConditions[i].contains("entity")){
				isValid = checkEntity(separatedConditions[i].split("=")[1], phrase);
			}
		}
		
		return isValid;
		
	}
	
	public static boolean checkEntity(String entity, String[] phrase){

		
		EntityExtractor eex = new EntityExtractor();
		HashMap<String, String> entityfound = eex.extract(phrase);
		if(entityfound!=null){
			System.out.println(entityfound);
			if(entityfound.containsValue(entity))
				return true;
		}
		
		return false;
	}
	
	
	public static String patternMatcher(String pattern,String text){

	      Pattern r = Pattern.compile(pattern);
	      Matcher m = r.matcher(text);
	      String expression = null;
	      if (m.find( )) {
	    	  expression = m.group(1);
	    	  return expression;
	      }
	      return null;
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
//		errPW.println("Pattern string:\n" + tregexPattern.pattern());
//		errPW.println("Parsed representation:");
//		tregexPattern.prettyPrint(errPW);

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
//		System.out.println(treebank.textualSummary());
		
//		errPW.println("There were " + vis.numMatches() + " matches in total.");
		List<Tree> matchedTrees = vis.getMatchedSubTrees();
		
		for (Tree tree : matchedTrees) {
//			System.out.println(tree.nodeString());
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
