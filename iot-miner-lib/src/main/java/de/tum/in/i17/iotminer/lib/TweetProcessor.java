package de.tum.in.i17.iotminer.lib;

import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.langdetect.Language;
import opennlp.tools.langdetect.LanguageDetector;
import opennlp.tools.langdetect.LanguageDetectorME;
import opennlp.tools.langdetect.LanguageDetectorModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.Span;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class TweetProcessor {

    public void detectLanguage(String tweet) throws IOException {
        InputStream stream = new ByteArrayInputStream(tweet.getBytes());
        LanguageDetectorModel m = new LanguageDetectorModel(stream);
        LanguageDetector myCategorizer = new LanguageDetectorME(m);

        // Get the most probable language
        Language bestLanguage = myCategorizer.predictLanguage(tweet);

    }

    public void nounPhrases(String tweet) throws IOException {
        SentenceDetectorME sentenceDetector;
        TokenizerME tokenizer;
        POSTaggerME posTagger;
        ChunkerME chunker;
        try (
                InputStream smis = this.getClass().getResourceAsStream("/opennlp/en-sent.bin");
                InputStream tmis = this.getClass().getResourceAsStream("/opennlp/en-token.bin");
                InputStream pmis = this.getClass().getResourceAsStream("/opennlp/en-pos-maxent.bin");
                InputStream cmis = this.getClass().getResourceAsStream("/opennlp/en-chunker.bin")) {
            SentenceModel smodel = new SentenceModel(smis);
            sentenceDetector = new SentenceDetectorME(smodel);
            TokenizerModel tmodel = new TokenizerModel(tmis);
            tokenizer = new TokenizerME(tmodel);
            POSModel pmodel = new POSModel(pmis);
            posTagger = new POSTaggerME(pmodel);
            ChunkerModel cmodel = new ChunkerModel(cmis);
            chunker = new ChunkerME(cmodel);

        }
//        String text = "This article provides a review of the literature on clinical correlates of awareness in dementia. Most inconsistencies were found with regard to an association between depression and higher levels of awareness. Dysthymia, but not major depression, is probably related to higher levels of awareness. Anxiety also appears to be related to higher levels of awareness. Apathy and psychosis are frequently present in patients with less awareness, and may share common neuropathological substrates with awareness. Furthermore, unawareness seems to be related to difficulties in daily life functioning, increased caregiver burden, and deterioration in global dementia severity. Factors that may be of influence on the inconclusive data are discussed, as are future directions of research.";
        final String text = tweet;
        Span[] sentSpans = sentenceDetector.sentPosDetect(text);
        for (Span sentSpan : sentSpans) {
            String sentence = sentSpan.getCoveredText(text).toString();
            int start = sentSpan.getStart();
            Span[] tokSpans = tokenizer.tokenizePos(sentence);
            String[] tokens = new String[tokSpans.length];
            for (int i = 0; i < tokens.length; i++) {
                tokens[i] = tokSpans[i].getCoveredText(sentence).toString();
            }
            String[] tags = posTagger.tag(tokens);
            Span[] chunks = chunker.chunkAsSpans(tokens, tags);
            System.out.println("----------------");
            for (Span chunk : chunks) {
                if ("NP".equals(chunk.getType())) {
                    int npstart = start + tokSpans[chunk.getStart()].getStart();
                    int npend = start + tokSpans[chunk.getEnd() - 1].getEnd();
                    System.out.println(text.substring(npstart, npend));
                }
            }
        }
    }
}
