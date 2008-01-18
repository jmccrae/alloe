package nii.alloe.corpus.analyzer;
/**
 * Modified from Lucene StandardAnalyzer
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.standard.*;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Set;

/**
 * Similar to StandardAnalyzer, but doesn't discard punctuation
 */
public class AlloeAnalyzer extends Analyzer {
  private Set stopSet;

  /** An array containing some common English words that are usually not
  useful for searching. */
  public static final String[] STOP_WORDS = StopAnalyzer.ENGLISH_STOP_WORDS;

  /** Builds an analyzer with the default stop words ({@link #STOP_WORDS}). */
  public AlloeAnalyzer() {
    this(STOP_WORDS);
  }

  /** Builds an analyzer with the given stop words. */
  public AlloeAnalyzer(Set stopWords) {
    stopSet = stopWords;
  }

  /** Builds an analyzer with the given stop words. */
  public AlloeAnalyzer(String[] stopWords) {
    stopSet = StopFilter.makeStopSet(stopWords);
  }

  /** Builds an analyzer with the stop words from the given file.
   * @see WordlistLoader#getWordSet(File)
   */
  public AlloeAnalyzer(File stopwords) throws IOException {
    stopSet = WordlistLoader.getWordSet(stopwords);
  }

  /** Builds an analyzer with the stop words from the given reader.
   * @see WordlistLoader#getWordSet(Reader)
   */
  public AlloeAnalyzer(Reader stopwords) throws IOException {
    stopSet = WordlistLoader.getWordSet(stopwords);
  }

  /** Constructs a {@link StandardTokenizer} filtered by a {@link
  StandardFilter}, a {@link LowerCaseFilter} and a {@link StopFilter}. */
  public TokenStream tokenStream(String fieldName, Reader reader) {
    TokenStream result = new AlloeTokenizer(reader);
    result = new StandardFilter(result);
    result = new LowerCaseFilter(result);
    result = new StopFilter(result, stopSet);
    return result;
  }
}
